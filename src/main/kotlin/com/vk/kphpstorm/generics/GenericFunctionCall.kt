package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.GenericFunctionUtil.genericNames
import com.vk.kphpstorm.generics.GenericFunctionUtil.isGeneric
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.helpers.toExPhpType
import kotlin.math.min

/**
 * Класс инкапсулирующий в себе всю логику обработки шаблонных вызовов.
 *
 * Он выводит неявные типы, когда нет явного определения списка типов при инстанциации.
 *
 * Например:
 *
 * ```php
 * /**
 *  * @kphp-generic T1, T2
 *  * @param T1 $a
 *  * @param T2 $b
 *  */
 * function f($a, $b) {}
 *
 * f(new A, new B); // => T1 = A, T2 = B
 * ```
 *
 * В случае когда есть явный список типов при инстанциации, он собирает типы
 * из него.
 *
 * Например:
 *
 * ```php
 * f/*<C, D>*/(new A, new B); // => T1 = C, T2 = D
 * ```
 */
class GenericFunctionCall(val call: FunctionReference) {
    val project = call.project
    var function: Function? = null
    val callArgs = call.parameters
    val parameters = mutableListOf<Parameter>()
    val genericTs = mutableListOf<String>()

    val explicitSpecs = mutableListOf<ExPhpType>()
    val implicitSpecs = mutableListOf<ExPhpType>()
    val specializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitSpecializationNameMap = mutableMapOf<String, ExPhpType>()
    val implicitSpecializationErrors = mutableMapOf<String, Pair<ExPhpType, ExPhpType>>()

    val explicitSpecsPsi = call.firstChild?.nextSibling?.takeIf {
        it is GenericInstantiationPsiCommentImpl
    } as? GenericInstantiationPsiCommentImpl

    init {
        init()
    }

    private fun init() {
        resolveFunction()
        if (function == null || !isGeneric()) return

//        function = call.resolve() as? Function
//        parameters.addAll(function!!.parameters)
//        genericTs.addAll(function!!.genericNames())

        // Несмотря на то, что явный список является превалирующим над
        // типами выведенными из аргументов функций, нам все равно
        // необходимы обв списка для дальнейших инспекций

        // В первую очередь, выводим все типы шаблонов из аргументов функции (при наличии)
        reifyAllGenericsT()
        // Далее, выводим все типы шаблонов из явного списка типов (при наличии)
        extractExplicitGenericsT()
    }

    /**
     * Having a call `f/*<A, B>*/(...)`, where `f` is `f<T1, T2>`, deduce T1 and T2 from
     * comment `/*<A, B>*/`.
     */
    private fun extractExplicitGenericsT() {
        resolveFunction()

        if (function == null) return
        if (explicitSpecsPsi == null) return

        val instances = explicitSpecsPsi.extractInstances()
        val specTypesString = explicitSpecsPsi.genericSpecs

        val lhsType = PhpType().add("PseudoClass$specTypesString").global(project)
        val parsed = lhsType.toExPhpType()

        val instantiation = when (parsed) {
            is ExPhpTypePipe -> parsed.items.firstOrNull { it is ExPhpTypeTplInstantiation }
            is ExPhpTypeNullable -> parsed.inner
            else -> parsed
        } as? ExPhpTypeTplInstantiation ?: return

        for (i in 0 until min(genericTs.size, instantiation.specializationList.size)) {
            val type = instantiation.specializationList[i]
            if (type is ExPhpTypeInstance) {
                val resolvedInstance = instances[type.fqn + i.toString()]
                val resolvedClasses = resolvedInstance?.classes(project)
                if (resolvedClasses != null && resolvedClasses.isNotEmpty()) {
                    val exType = if (resolvedClasses.size > 1) {
                        ExPhpTypePipe(resolvedClasses.map { ExPhpTypeInstance(it.fqn) })
                    } else {
                        ExPhpTypeInstance(resolvedClasses.first().fqn)
                    }

                    explicitSpecs.add(exType)
                    specializationNameMap[genericTs[i]] = exType
                    continue
                }
            }

            explicitSpecs.add(instantiation.specializationList[i])
            specializationNameMap[genericTs[i]] = instantiation.specializationList[i]
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
     * This function is called for every template argument of `f()` invocation.
     *
     * TODO: add callable support
     */
    private fun reifyArgumentGenericsT(arg: PsiElement, argExType: ExPhpType, paramExType: ExPhpType) {
        if (arg !is PhpTypedElement) return

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
                reifyArgumentGenericsT(arg, argExType.inner, paramExType.inner)
            } else {
                reifyArgumentGenericsT(arg, argExType, paramExType.inner)
            }
        }

        if (paramExType is ExPhpTypePipe) {
            // если случай T|false
            if (paramExType.items.size == 2 && paramExType.items.any { it == ExPhpType.FALSE }) {
                if (argExType is ExPhpTypePipe) {
                    val argTypeWithoutFalse = ExPhpTypePipe(argExType.items.filter { it != ExPhpType.FALSE })
                    val paramTypeWithoutFalse = paramExType.items.first { it != ExPhpType.FALSE }
                    reifyArgumentGenericsT(arg, argTypeWithoutFalse, paramTypeWithoutFalse)
                }
                // TODO: подумать над пайпами, так как не все так очевидно
            }
        }

        if (paramExType is ExPhpTypeClassString) {
            if (arg is ClassConstantReference) {
                val classExType = arg.classReference?.type?.toExPhpType()
                if (classExType != null) {
                    reifyArgumentGenericsT(arg, classExType, paramExType.inner)
                }
            }

            val isPipeWithClassString = argExType is ExPhpTypePipe &&
                    argExType.items.any { it is ExPhpTypeClassString }

            // Для случаев когда нативный вывод типов дает в результате string|class-string<Boo>
            // В таком случае нам необходимо вычленить более точный тип.
            val classStringType = if (isPipeWithClassString) {
                (argExType as ExPhpTypePipe).items.find { it is ExPhpTypeClassString }
            } else if (argExType is ExPhpTypeClassString) {
                argExType
            } else {
                null
            }

            if (classStringType is ExPhpTypeClassString) {
                reifyArgumentGenericsT(arg, classStringType.inner, paramExType.inner)
            }
        }

        if (paramExType is ExPhpTypeArray) {
            if (arg is ArrayCreationExpression) {
                ArrayCreationExpressionImpl.children(arg).forEach { el ->
                    if (el is ArrayHashElement) {
                        if (el.value != null && el.value is PhpTypedElement) {
                            val elExType = (el.value as PhpTypedElement).type.toExPhpType() ?: return@forEach
                            reifyArgumentGenericsT(el.value!!, elExType, paramExType.inner)
                        }
                    } else if (el.firstChild is PhpTypedElement) {
                        val elExType = (el.firstChild as PhpTypedElement).type.toExPhpType() ?: return@forEach
                        reifyArgumentGenericsT(el.firstChild, elExType, paramExType.inner)
                    }
                }
            }
        }

        if (paramExType is ExPhpTypeTuple) {
            if (argExType is ExPhpTypeTuple) {
                for (i in 0 until min(argExType.items.size, paramExType.items.size)) {
                    reifyArgumentGenericsT(arg, argExType.items[i], paramExType.items[i])
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

                    reifyArgumentGenericsT(arg, argShapeItem.type, correspondingParamShapeItem.type)
                }
            }
        }
    }

    /**
     * Having a call `f(...)` of a template function `f<T1, T2>(...)`, deduce T1 and T2
     * "auto deducing" for generics arguments is typically called "reification".
     */
    private fun reifyAllGenericsT() {
        for (i in 0 until min(callArgs.size, parameters.size)) {
            val param = parameters[i] as? PhpTypedElement ?: continue
            val paramType = param.type.global(project)
            val paramExType = paramType.toExPhpType() ?: continue

            // Если параметр не является шаблонным, то мы его пропускаем
            if (!paramType.isGeneric(genericTs)) {
                continue
            }

            val arg = callArgs[i] as? PhpTypedElement ?: continue
            val argExType = arg.type.global(project).toExPhpType() ?: continue
            reifyArgumentGenericsT(arg, argExType, paramExType)
        }
    }

    fun resolveFunction() {
        if (function == null) {
            function = call.resolve() as? Function ?: return
            parameters.addAll(function!!.parameters)
            genericTs.addAll(function!!.genericNames())
        }
    }

    fun withExplicitSpecs(): Boolean {
        return explicitSpecsPsi != null
    }

    fun isGeneric(): Boolean {
        return function?.isGeneric() == true
    }

    /**
     * Функция проверяющая, что явно указанные шаблонные типы
     * соответствуют автоматически выведенным типам и их можно
     * безопасно удалить.
     *
     * Например:
     *
     * ```php
     * /**
     *  * @kphp-generic T
     *  * @param T $arg
     *  */
     * function f($arg) {}
     *
     * f/*<Foo>*/(new Foo); // === f(new Foo);
     * ```
     */
    fun isNoNeedExplicitSpec(): Boolean {
        if (function == null) return false
        if (explicitSpecsPsi == null) return false

        val countGenericNames = function!!.genericNames().size
        val countExplicitSpecs = explicitSpecs.size
        val countImplicitSpecs = implicitSpecs.size

        if (countGenericNames == countExplicitSpecs && countExplicitSpecs == countImplicitSpecs) {
            var isEqual = true
            explicitSpecs.forEachIndexed { index, explicitSpec ->
                if (!implicitSpecs[index].isAssignableFrom(explicitSpec, project)) {
                    isEqual = false
                }
            }
            return isEqual
        }

        return false
    }

    /**
     * Имея следующую функцию:
     *
     * ```php
     * /**
     *  * @kphp-generic T
     *  * @param T $arg
     *  */
     * function f($arg) {}
     * ```
     *
     * И следующий вызов:
     *
     * ```php
     * f/*<Foo>*/(new Foo);
     * ```
     *
     * Нам необходимо вывести тип `$arg`, для того чтобы проверить, что
     * переданное выражение `new Foo` имеет правильный тип.
     *
     * Так как функция может вызываться с разными шаблонными типа, нам
     * необходимо найти тип `$arg` для каждого конкретного вызова.
     * В примере выше в результате будет возвращен тип `Foo`.
     */
    fun typeOfParam(index: Int): ExPhpType? {
        if (function == null) return null

        val param = function!!.getParameter(index) ?: return null
        val paramType = param.type
        if (paramType.isGeneric(function!!)) {
            val usedNameMap = specializationNameMap.ifEmpty {
                implicitSpecializationNameMap
            }
            return paramType.toExPhpType()?.instantiateGeneric(usedNameMap)
        }

        return null
    }

    override fun toString(): String {
        val specs = explicitSpecs.ifEmpty { implicitSpecs }
        return "${function?.fqn ?: "UnknownFunction"}<${specs.joinToString(",")}>"
    }
}
