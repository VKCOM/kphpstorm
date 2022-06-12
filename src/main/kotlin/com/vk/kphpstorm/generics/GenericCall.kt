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
 * Is a union of the [IndexingGenericCall] and [ResolvingGenericCallBase] classes, which
 * can be used for checks after PhpStorm has completed the indexing.
 *
 * Note: can't be used during indexing!
 *
 * [GenericCall] encapsulates all the logic for processing generic calls.
 *
 * It infers implicit types when no explicit type list definition on instantiation.
 *
 * For example:
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
 * In the case where an explicit instantiation list of types, it collects the types from it.
 *
 * For example:
 *
 * ```php
 * f/*<C, D>*/(new A, new B); // => T1 = C, T2 = D
 * ```
 */
abstract class GenericCall(val project: Project) {
    abstract val element: PsiElement
    abstract val arguments: Array<PsiElement>
    abstract val argumentTypes: List<ExPhpType?>
    abstract val klass: PhpClass?

    val explicitSpecsPsi: GenericInstantiationPsiCommentImpl? by lazy {
        GenericUtil.findInstantiationComment(element)
    }
    private var contextType: ExPhpType? = null

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

    private val genericTs = mutableListOf<KphpDocGenericParameterDecl>()
    private val parameters = mutableListOf<Parameter>()

    private val extractor = GenericInstantiationExtractor()
    protected val reifier = GenericReifier(project)

    val explicitSpecs get() = extractor.explicitSpecs
    val specializationNameMap get() = extractor.specializationNameMap
    val implicitSpecs get() = reifier.implicitSpecs
    val implicitSpecializationNameMap get() = reifier.implicitSpecializationNameMap
    val implicitSpecializationErrors get() = reifier.implicitSpecializationErrors

    protected fun init() {
        val function = function() ?: return
        if (!isGeneric()) return

        val genericNames = genericNames()

        parameters.addAll(function.parameters)
        genericTs.addAll(genericNames)

        // If the current call is in return or is a function argument,
        // then we can extract additional type hints.
        contextType = calcContextType(element)

        // Even though the explicit list takes precedence over types inferred
        // from function arguments, we still need both lists for further inspections.

        // First, we reify all generic types from the function arguments, if any.
        reifier.reifyAllGenericsT(klass, function.parameters, genericNames, argumentTypes, contextType)
        // Next, we extract all explicit generic types from the explicit list of types, if any.
        extractor.extractExplicitGenericsT(genericNames(), explicitSpecsPsi)
    }

    private fun calcContextType(element: PsiElement): ExPhpType? {
        val parent = element.parent ?: return null
        if (parent is PhpReturn) {
            val parentFunction = parent.parentOfType<Function>() ?: return null
            val returnType = parentFunction.docComment?.returnTag?.type
            return returnType?.toExPhpType()
        }

        if (parent is ParameterList) {
            val calledInFunctionCall = parent.parentOfType<FunctionReference>() ?: return null

            val calledFunction = calledInFunctionCall.resolve() as? Function ?: return null
            val index = parent.parameters.indexOf(element)

            calledFunction.getParameter(index)?.let {
                return it.type.toExPhpType()
            }
        }

        return null
    }

    fun withExplicitSpecs() = explicitSpecsPsi != null

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
