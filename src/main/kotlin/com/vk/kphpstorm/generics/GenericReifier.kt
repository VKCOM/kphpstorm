package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.GenericUtil.genericInheritInstantiation
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.genericParents
import com.vk.kphpstorm.generics.GenericUtil.getGenericTypeOrSelf
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import kotlin.math.min

/**
 * Encapsulates generic type inference logic from function parameters.
 */
class GenericReifier(val project: Project) {
    val implicitSpecs = mutableListOf<ExPhpType>()
    val implicitSpecializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitClassSpecializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitSpecializationErrors = mutableMapOf<String, Pair<ExPhpType, ExPhpType>>()

    /**
     * Having a call `f(...)` of a generic function `f<T1, T2>(...)`, deduce T1 and T2
     * "auto deducing" for generics arguments is typically called "reification".
     */
    fun reifyAllGenericsT(
        klass: PhpClass?,
        parameters: Array<Parameter>,
        genericTs: List<KphpDocGenericParameterDecl>,
        argumentTypes: List<ExPhpType?>,
        contextType: ExPhpType?,
    ) {
        for (i in 0 until min(argumentTypes.size, parameters.size)) {
            val param = parameters[i] as? PhpTypedElement ?: continue
            val paramType = param.type.global(project)
            // Когда мы примешиваем extends или default тип, то появляется pipe тип,
            // поэтому нам необходимо достать из него generic тип для разрешения.
            val paramExType = paramType.toExPhpType()?.getGenericTypeOrSelf() ?: continue

            // Если параметр не является шаблонным, то мы его пропускаем
            if (!paramType.isGeneric(genericTs) && paramType.toString() != KphpPrimitiveTypes.CALLABLE) {
                continue
            }

            val argExType = argumentTypes[i] ?: continue
            reifyArgumentGenericsT(argExType, paramExType)
        }

        // TODO: А что если тут несколько инстанциаций в типе?
        val instantiation = contextType?.getInstantiation()
        if (instantiation != null) {
            val specList = instantiation.specializationList
            for (i in 0 until min(specList.size, genericTs.size)) {
                val type = specList[i]
                val genericT = genericTs[i]

                implicitSpecializationNameMap[genericT.name] = type
            }
        }

        genericTs.forEach {
            if (it.defaultType != null) {
                // Если тип для параметра уже выведен, то пропускаем его.
                if (implicitSpecializationNameMap.containsKey(it.name)) {
                    return@forEach
                }

                val defaultType = if (it.defaultType.isGeneric()) {
                    it.defaultType.instantiateGeneric(implicitSpecializationNameMap)
                } else {
                    it.defaultType
                }

                implicitSpecializationNameMap[it.name] = defaultType
            }
        }

        implicitSpecializationNameMap.forEach { (_, type) ->
            implicitSpecs.add(type)
        }
        implicitSpecializationNameMap.putAll(implicitClassSpecializationNameMap)

        if (klass != null) {
            val parentsList = klass.genericParents()
            parentsList.forEach { parent ->
                val extendsName = parent.fqn
                val genericNames = parent.genericNames()
                val inheritInstantiation = klass.genericInheritInstantiation(extendsName)
                if (inheritInstantiation != null) {
                    val specList = inheritInstantiation.specializationList()

                    val classSpecializationMap = genericNames.associate {
                        it.name to it.defaultType
                    }.toMutableMap()

                    for (i in 0 until Integer.min(genericNames.size, specList.size)) {
                        val genericT = genericNames[i]
                        val spec = specList[i]

                        classSpecializationMap[genericT.name] = spec
                    }

                    classSpecializationMap.forEach classForEach@{ (name, type) ->
                        if (type == null) {
                            return@classForEach
                        }
                        implicitSpecializationNameMap[name] = type.instantiateGeneric(implicitSpecializationNameMap)
                    }
                }
            }
        }
    }

    /**
     * Having a call `f($arg)`, where `f` is `f<T>`, and `$arg` is `@param T[] $array`, deduce T.
     *
     * For example:
     * 1. if `@param T[]` and `$arg` is `A[]`, then T is A
     * 2. if `@param class-string<T>` and `$arg` is `class-string<A>`, then T is A
     * 3. if `@param shape(key: T)` and `$arg` is `shape(key: A)`, then T is A
     *
     * This function is called for every generic argument of `f()` invocation.
     */
    private fun reifyArgumentGenericsT(argExType: ExPhpType, paramExType: ExPhpType) {
        if (paramExType is ExPhpTypeGenericsT) {
            val prevReifiedType = implicitSpecializationNameMap[paramExType.nameT]
            // Особые случаи, когда несколько типов это не ошибка
            val isAny = argExType == ExPhpType.ANY || prevReifiedType == ExPhpType.ANY
            val isNull = argExType == ExPhpType.NULL || prevReifiedType == ExPhpType.NULL
            if (prevReifiedType != null && !isAny && !isNull && prevReifiedType.toString() != argExType.toString()) {
                // В таком случае мы получаем ситуацию когда один шаблонный тип
                // имеет несколько возможных вариантов типа, что является ошибкой.
                implicitSpecializationErrors[paramExType.nameT] = Pair(argExType, prevReifiedType)
            }

            if (prevReifiedType == null || argExType != ExPhpType.ANY && argExType != ExPhpType.NULL) {
                implicitSpecializationNameMap[paramExType.nameT] = argExType
            }
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
                paramExType.items.find { it is ExPhpTypeTplInstantiation } as? ExPhpTypeTplInstantiation
            val instantiationArgType =
                if (argExType is ExPhpTypePipe)
                    argExType.items.find { it is ExPhpTypeTplInstantiation } as? ExPhpTypeTplInstantiation
                else
                    argExType as? ExPhpTypeTplInstantiation

            if (instantiationParamType != null && instantiationArgType != null) {
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
