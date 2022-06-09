package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeTplInstantiationPsiImpl

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

    fun className(): String? {
        val instantiation = findChildByClass(ExPhpTypeTplInstantiationPsiImpl::class.java) ?: return null
        return instantiation.type.types.firstOrNull { !it.contains("(") }
    }
}
