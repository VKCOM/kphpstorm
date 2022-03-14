package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl

/**
 * Inside '@kphp-generic T1, T2: ExtendsClass' — 'T1' and 'T2: ExtendsClass' are separate psi elements of this impl
 * @see KphpDocTagGenericElementType.getTagParser
 */
class KphpDocGenericParameterDeclPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node) {
    companion object {
        val elementType = PhpDocElementType("phpdocGenericParameterDecl")
    }

    override fun getName(): String {
        val text = text
        if (text.contains(':')) {
            return text.substring(0 until text.indexOf(':'))
        }

        return text
    }

    fun getExtendsClass(): String? {
        if (text.contains(':')) {
            return text.substring(text.indexOf(':') + 1)
        }

        return null
    }
}
