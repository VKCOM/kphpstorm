package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl

class KphpDocJsonForEncoderPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node) {
    companion object {
        val elementType = PhpDocElementType("phpdocJsonFor")
    }

    fun name(): String? {
        return node.lastChildNode?.text
    }
}
