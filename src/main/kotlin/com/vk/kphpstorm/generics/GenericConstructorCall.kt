package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl

class GenericConstructorCall(call: NewExpression) : GenericCall(call.project) {
    override val element = call
    override val arguments: Array<PsiElement> = call.parameters
    override val argumentTypes: List<ExPhpType?> = arguments
        .filterIsInstance<PhpTypedElement>().map { it.type.global(project).toExPhpType() }

    override val klass: PhpClass?
    private val method: Method?

    init {
        val className = call.classReference?.fqn
        klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull()
        // If the class doesn't have a constructor, then we create its pseudo version.
        method = klass?.constructor ?: createPseudoConstructor(project, klass?.name ?: "UnknownClass")

        init()
    }

    override fun function() = method

    override fun isResolved() = method != null && klass != null

    override fun genericNames(): List<KphpDocGenericParameterDecl> {
        val methodNames = method?.genericNames() ?: emptyList()
        val classNames = klass?.genericNames() ?: emptyList()

        return mutableListOf<KphpDocGenericParameterDecl>()
            .apply { addAll(methodNames) }
            .apply { addAll(classNames) }
            .toList()
    }

    override fun ownGenericNames() = genericNames()

    override fun isGeneric() = genericNames().isNotEmpty()

    override fun toString(): String {
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${klass?.fqn ?: "UnknownClass"}->__construct<$explicit>($implicit)"
    }

    private fun createPseudoConstructor(project: Project, className: String): Method {
        return PhpPsiElementFactory.createPhpPsiFromText(
            project,
            Method::class.java, "class $className { public function __construct() {} }"
        )
    }
}
