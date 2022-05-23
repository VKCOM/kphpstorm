package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl

data class KphpDocGenericParameterDecl(val name: String, val extendsClass: String? = null)

/**
 * Inside '@kphp-generic T1, T2: ExtendsClass' — 'T1' and 'T2: ExtendsClass' are separate psi elements of this impl
 * @see KphpDocTagGenericElementType.getTagParser
 */
class KphpDocGenericParameterDeclPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node), PhpDocRef {
    companion object {
        val elementType = PhpDocElementType("phpdocGenericParameterDecl")
    }

    private val name = text.substringBefore(':')
    private val extendsClass = findChildByClass(ExPhpTypeInstancePsiImpl::class.java)

    override fun getName() = name
    fun getExtendsClass() = extendsClass

    fun decl(): KphpDocGenericParameterDecl {
        val extendsClassRef = extendsClass?.resolveLocal()?.firstOrNull()
        val fqn = extendsClassRef?.fqn ?: extendsClass?.fqn
        return KphpDocGenericParameterDecl(name, fqn)
    }
}
