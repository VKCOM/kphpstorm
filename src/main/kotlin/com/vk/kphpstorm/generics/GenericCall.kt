package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl

/**
 * Ввиду причин описанных в [IndexingGenericFunctionCall], мы не можем использовать
 * объединенный класс для обработки вызова во время вывода типов. Однако в других
 * местах мы можем использовать индекс и поэтому нам не нужно паковать данные и
 * потом их распаковывать, мы можем делать все за раз.
 *
 * Данный класс является объединением [IndexingGenericFunctionCall] и
 * [ResolvingGenericFunctionCall] и может быть использован для обработки шаблонных
 * вызовов в других местах;
 *
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
abstract class GenericCall(val project: Project) {
    abstract val callArgs: Array<PsiElement>
    abstract val explicitSpecsPsi: GenericInstantiationPsiCommentImpl?
    abstract val argumentsTypes: List<ExPhpType?>
    abstract val klass: PhpClass?
    protected var contextType: ExPhpType? = null

    abstract fun element(): PsiElement
    abstract fun function(): Function?
    abstract fun isResolved(): Boolean

    /**
     * Returns generic parameters belonging to the current element,
     * and, if it's part of a class, class parameter as well.
     */
    abstract fun genericNames(): List<KphpDocGenericParameterDecl>

    /**
     * Returns generic parameters belonging only to the current element.
     * If it's a method, for example, then only the method parameters are
     * returned without the class parameters.
     */
    abstract fun ownGenericNames(): List<KphpDocGenericParameterDecl>

    abstract fun isGeneric(): Boolean

    val genericTs = mutableListOf<KphpDocGenericParameterDecl>()
    private val parameters = mutableListOf<Parameter>()

    protected val extractor = GenericInstantiationExtractor()
    protected val reifier = GenericsReifier(project)

    val explicitSpecs get() = extractor.explicitSpecs
    val specializationNameMap get() = extractor.specializationNameMap
    val implicitSpecs get() = reifier.implicitSpecs
    val implicitSpecializationNameMap get() = reifier.implicitSpecializationNameMap
    val implicitClassSpecializationNameMap get() = reifier.implicitClassSpecializationNameMap
    val implicitSpecializationErrors get() = reifier.implicitSpecializationErrors

    protected fun init() {
        val function = function() ?: return
        if (!isGeneric()) return

        val genericNames = genericNames()

        parameters.addAll(function.parameters)
        genericTs.addAll(genericNames)

        // Если текущий вызов находится в return или является аргументом
        // функции, то мы можем извлечь дополнительные подсказки по типам.
        calcContextType(element())

        // Несмотря на то, что явный список является превалирующим над
        // типами выведенными из аргументов функций, нам все равно
        // необходимы обв списка для дальнейших инспекций

        // В первую очередь, выводим все типы шаблонов из аргументов функции (при наличии)
        reifier.reifyAllGenericsT(klass, function.parameters, genericNames, argumentsTypes, contextType)
        // Далее, выводим все типы шаблонов из явного списка типов (при наличии)
        extractor.extractExplicitGenericsT(genericNames(), explicitSpecsPsi)
    }

    private fun calcContextType(element: PsiElement) {
        val parent = element.parent
        if (parent is PhpReturn) {
            val parentFunction = parent.parentOfType<Function>()
            if (parentFunction != null) {
                val returnType = parentFunction.docComment?.returnTag?.type
                contextType = returnType?.toExPhpType()
            }

            return
        }

        if (parent is ParameterList) {
            val calledInFunctionCall = parent.parentOfType<FunctionReference>()
            if (calledInFunctionCall != null) {
                val calledFunction = calledInFunctionCall.resolve() as? Function
                if (calledFunction != null) {
                    val index = parent.parameters.indexOf(element)
                    calledFunction.getParameter(index)?.let {
                        contextType = it.type.toExPhpType()
                    }
                }
            }
        }
    }

    fun withExplicitSpecs(): Boolean {
        return explicitSpecsPsi != null
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
        val function = function() ?: return null

        val param = function.getParameter(index) ?: return null
        val paramType = param.type
        if (paramType.isGeneric(genericNames())) {
            val usedNameMap = extractor.specializationNameMap.ifEmpty {
                reifier.implicitSpecializationNameMap
            }
            return paramType.toExPhpType()?.instantiateGeneric(usedNameMap)
        }

        return null
    }

    fun isNotEnoughInformation(): KphpDocGenericParameterDecl? {
        if (explicitSpecsPsi != null) return null

        val genericNames = if (this is GenericMethodCall && isStatic()) {
            ownGenericNames()
        } else {
            genericNames()
        }

        genericNames.forEach { decl ->
            val resolved = implicitSpecializationNameMap.contains(decl.name)

            if (!resolved) {
                return decl
            }
        }

        return null
    }

    abstract override fun toString(): String
}
