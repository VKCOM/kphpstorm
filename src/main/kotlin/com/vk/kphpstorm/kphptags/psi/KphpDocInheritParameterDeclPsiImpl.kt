package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeInstance
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeTplInstantiationPsiImpl
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
import com.vk.kphpstorm.helpers.toExPhpType

data class KphpDocInheritParameterDecl(
    val name: String?,
    val specializationList: List<String>,
    val text: String,
) {
    fun specializationList(): List<ExPhpType> {
        return specializationList.map { PhpType().add(it).toExPhpType() ?: ExPhpType.ANY }
    }

    override fun toString() = text
}

/**
 * Inside '@kphp-inherit ExtendsClass<Type>, ImplementsClass<Type>' — 'ExtendsClass<Type>' and 'ImplementsClass<Type'
 * are separate psi elements of this impl.
 *
 * @see KphpDocTagInheritElementType.getTagParser
 */
class KphpDocInheritParameterDeclPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node), PhpDocRef {
    companion object {
        val elementType = PhpDocElementType("phpdocInheritParameterDecl")
    }

    fun type() = findChildByClass(PhpDocTypeImpl::class.java)

    fun decl(): KphpDocInheritParameterDecl {
        return KphpDocInheritParameterDecl(
            className(),
            specializationList().map { it.toString() },
            text,
        )
    }

    fun className(): String? {
        val instantiation = findChildByClass(PhpDocTypeImpl::class.java) ?: return null
        if (instantiation !is ExPhpTypeTplInstantiationPsiImpl && instantiation !is ExPhpTypeInstancePsiImpl)
            return null

        val exType = instantiation.type.toExPhpType() ?: return null
        if (exType is ExPhpTypeTplInstantiation)
            return exType.classFqn
        if (exType is ExPhpTypeInstance)
            return exType.fqn

        return exType.getInstantiation()?.classFqn
    }

    private fun specializationList(): List<ExPhpType> {
        val instantiationPsi = findChildByClass(PhpDocTypeImpl::class.java) ?: return emptyList()
        if (instantiationPsi !is ExPhpTypeTplInstantiationPsiImpl)
            return emptyList()

        val exType = instantiationPsi.type.toExPhpType() ?: return emptyList()
        val instantiation = exType.getInstantiation() ?: return emptyList()

        return instantiation.specializationList
    }
}
