package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import kotlin.math.min

/**
 * Класс инкапсулирующий логику вывода шаблонных типов из параметров функций.
 */
class GenericsReifier(val project: Project) {
    val implicitSpecs = mutableListOf<ExPhpType>()
    val implicitSpecializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitClassSpecializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitSpecializationErrors = mutableMapOf<String, Pair<ExPhpType, ExPhpType>>()

    /**
     * Having a call `f(...)` of a template function `f<T1, T2>(...)`, deduce T1 and T2
     * "auto deducing" for generics arguments is typically called "reification".
     */
    fun reifyAllGenericsT(
        klass: PhpClass?,
        parameters: Array<Parameter>,
        genericTs: List<KphpDocGenericParameterDecl>,
        argumentsTypes: List<ExPhpType?>,
        contextType: ExPhpType?,
    ) {
        for (i in 0 until min(argumentsTypes.size, parameters.size)) {
            val param = parameters[i] as? PhpTypedElement ?: continue
            val paramType = param.type.global(project)
            val paramExType = paramType.toExPhpType() ?: continue

            // Если параметр не является шаблонным, то мы его пропускаем
            if (!paramType.isGeneric(genericTs)) {
                continue
            }

            val argExType = argumentsTypes[i] ?: continue
            reifyArgumentGenericsT(argExType, paramExType)
        }

        var instantiation = contextType?.getInstantiation()
        if (instantiation != null) {
            // TODO: сделать верный вывод типов тут
            if (instantiation.classFqn == klass?.fqn) {
                instantiation = instantiation.specializationList.firstOrNull() as? ExPhpTypeTplInstantiation
            }

            if (instantiation != null) {
                val specList = instantiation.specializationList
                for (i in 0 until min(specList.size, genericTs.size)) {
                    val type = specList[i]
                    val genericT = genericTs[i]

                    implicitSpecializationNameMap[genericT.name] = type
                }
            }
        }

        implicitSpecializationNameMap.putAll(implicitClassSpecializationNameMap)
    }

    /**
     * Having a call `f($arg)`, where `f` is `f<T>`, and `$arg` is `@param T[] $array`, deduce T.
     *
     * For example:
     * 1. if `@param T[]` and `$arg` is `A[]`, then T is A
     * 2. if `@param class-string<T>` and `$arg` is `class-string<A>`, then T is A
     * 3. if `@param shape(key: T)` and `$arg` is `shape(key: A)`, then T is A
     *
     * This function is called for every template argument of `f()` invocation.
     *
     * TODO: add callable support
     */
    private fun reifyArgumentGenericsT(argExType: ExPhpType, paramExType: ExPhpType) {
        if (paramExType is ExPhpTypeGenericsT) {
            val prevReifiedType = implicitSpecializationNameMap[paramExType.nameT]
            if (prevReifiedType != null) {
                // В таком случае мы получаем ситуацию когда один шаблонный тип
                // имеет несколько возможных вариантов типа, что является ошибкой.
                implicitSpecializationErrors[paramExType.nameT] = Pair(argExType, prevReifiedType)
            }

            implicitSpecializationNameMap[paramExType.nameT] = argExType
            implicitSpecs.add(argExType)
        }

        if (paramExType is ExPhpTypeNullable) {
            if (argExType is ExPhpTypeNullable) {
                reifyArgumentGenericsT(argExType.inner, paramExType.inner)
            } else {
                reifyArgumentGenericsT(argExType, paramExType.inner)
            }
        }

        if (paramExType is ExPhpTypePipe) {
            // если случай paramExType это Vector|Vector<%T> и argExType это Vector|Vector<A>
            val instantiationParamType =
                paramExType.items.find { it is ExPhpTypeTplInstantiation } as ExPhpTypeTplInstantiation?
            if (instantiationParamType != null && argExType is ExPhpTypePipe) {
                val instantiationArgType =
                    argExType.items.find { it is ExPhpTypeTplInstantiation } as ExPhpTypeTplInstantiation?
                if (instantiationArgType != null) {
                    for (i in 0 until min(
                        instantiationArgType.specializationList.size,
                        instantiationParamType.specializationList.size
                    )) {
                        reifyArgumentGenericsT(
                            instantiationArgType.specializationList[i],
                            instantiationParamType.specializationList[i]
                        )
                    }
                }
            }

            // если случай T|false
            if (paramExType.items.size == 2 && paramExType.items.any { it == ExPhpType.FALSE }) {
                if (argExType is ExPhpTypePipe) {
                    val argTypeWithoutFalse = ExPhpTypePipe(argExType.items.filter { it != ExPhpType.FALSE })
                    val paramTypeWithoutFalse = paramExType.items.first { it != ExPhpType.FALSE }
                    reifyArgumentGenericsT(argTypeWithoutFalse, paramTypeWithoutFalse)
                }
                // TODO: подумать над пайпами, так как не все так очевидно
            }
        }

        if (paramExType is ExPhpTypeCallable) {
            if (argExType is ExPhpTypeCallable) {
                if (argExType.returnType != null && paramExType.returnType != null) {
                    reifyArgumentGenericsT(argExType.returnType, paramExType.returnType)
                }
                for (i in 0 until min(argExType.argTypes.size, paramExType.argTypes.size)) {
                    reifyArgumentGenericsT(argExType.argTypes[i], paramExType.argTypes[i])
                }
            }
        }

        if (paramExType is ExPhpTypeClassString) {
            val isPipeWithClassString = argExType is ExPhpTypePipe &&
                    argExType.items.any { it is ExPhpTypeClassString }

            // Для случаев когда нативный вывод типов дает в результате string|class-string<Boo>
            // В таком случае нам необходимо найти более точный тип.
            val classStringType = if (isPipeWithClassString) {
                (argExType as ExPhpTypePipe).items.find { it is ExPhpTypeClassString }
            } else if (argExType is ExPhpTypeClassString) {
                argExType
            } else {
                null
            }

            if (classStringType is ExPhpTypeClassString) {
                reifyArgumentGenericsT(classStringType.inner, paramExType.inner)
            }
        }

        if (paramExType is ExPhpTypeArray) {
            if (argExType is ExPhpTypeArray) {
                reifyArgumentGenericsT(argExType.inner, paramExType.inner)
            }
        }

        if (paramExType is ExPhpTypeTuple) {
            if (argExType is ExPhpTypeTuple) {
                for (i in 0 until min(argExType.items.size, paramExType.items.size)) {
                    reifyArgumentGenericsT(argExType.items[i], paramExType.items[i])
                }
            }
        }

        if (paramExType is ExPhpTypeShape) {
            val isPipeWithShapes = argExType is ExPhpTypePipe &&
                    argExType.items.any { it is ExPhpTypeShape && it.items.isNotEmpty() }

            // Для случаев когда нативный вывод типов дает в результате shape()|shape(key1:Foo...)
            // В таком случае нам необходимо вычленить более точный шейп.
            val shapeWithKeys = if (isPipeWithShapes) {
                (argExType as ExPhpTypePipe).items.find { it is ExPhpTypeShape && it.items.isNotEmpty() }
            } else if (argExType is ExPhpTypeShape) {
                argExType
            } else {
                null
            }

            if (shapeWithKeys is ExPhpTypeShape) {
                shapeWithKeys.items.forEach { argShapeItem ->
                    val correspondingParamShapeItem = paramExType.items.find { paramShapeItem ->
                        argShapeItem.keyName == paramShapeItem.keyName
                    } ?: return@forEach

                    reifyArgumentGenericsT(argShapeItem.type, correspondingParamShapeItem.type)
                }
            }
        }
    }
}
