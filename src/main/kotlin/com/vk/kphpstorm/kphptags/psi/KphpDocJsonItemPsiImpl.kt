package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl

class KphpDocJsonItemPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node) {
    private val parts = node.text.split('=')

    fun name(): String {
        return parts[0].trimEnd()
    }

    fun stringValue(): String? {
        return parts.getOrNull(1)?.trimStart()
    }
    
    fun intValue(): Int? {
        return stringValue()?.toIntOrNull()
    }

    companion object {
        val elementType = PhpDocElementType("phpdocJsonItem")
    }
}
