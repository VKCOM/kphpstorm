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

class GenericConstructorCall(private val call: NewExpression) : GenericCall(call.project) {
    override val callArgs: Array<PsiElement> = call.parameters
    override val argumentsTypes: List<ExPhpType?> = callArgs
        .filterIsInstance<PhpTypedElement>().map { it.type.global(project).toExPhpType() }
    override val explicitSpecsPsi = GenericUtil.findInstantiationComment(call)

    private val klass: PhpClass?
    private val method: Method?

    init {
        val className = call.classReference?.fqn
        klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull()
        val constructor = klass?.constructor

        // Если у класса нет конструктора, то создаем его псевдо версию
        method = constructor ?: createPseudoConstructor(project, klass?.name ?: "Foo")

        init()
    }

    override fun element() = call
    override fun function() = method

    override fun isResolved() = method != null && klass != null

    override fun genericNames(): List<KphpDocGenericParameterDecl> {
        val methodsNames = method?.genericNames() ?: emptyList()
        val classesNames = klass?.genericNames() ?: emptyList()

        return mutableListOf<KphpDocGenericParameterDecl>()
            .apply { addAll(methodsNames) }
            .apply { addAll(classesNames) }
            .toList()
    }

    override fun ownGenericNames(): List<KphpDocGenericParameterDecl> {
        return klass?.genericNames() ?: emptyList()
    }

    override fun isGeneric() = genericNames().isNotEmpty()

    override fun toString(): String {
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${klass?.fqn ?: "UnknownClass"}->__construct<$explicit>($implicit)"
    }

    private fun createPseudoConstructor(project: Project, className: String): Method {
        return PhpPsiElementFactory.createPhpPsiFromText(
            project,
            Method::class.java, "class $className{ public function __construct() {} }"
        )
    }
}
