package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl

/**
 * Inside '@kphp-template-class T1, T2' — 'T1' and 'T2' are separate psi elements of this impl
 * @see KphpDocTagTemplateClassElementType.getTagParser
 */
class KphpDocTplParameterDeclPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node) {
    companion object {
        val elementType = PhpDocElementType("phpdocTplParameterDecl")
    }

    override fun getName(): String? = text
}
