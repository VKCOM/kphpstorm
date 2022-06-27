package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl

class KphpDocJsonPropertyPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node) {
    companion object {
        val elementType = PhpDocElementType("phpdocJsonItem")
    }

    private val parts = node.text.split('=')

    fun name() = parts[0].trimEnd()

    fun stringValue() = parts.getOrNull(1)?.trimStart()

    fun intValue() = stringValue()?.toIntOrNull()

    fun booleanValue(): Boolean? {
        val value = stringValue() ?: return true

        if (value.isEmpty() || value == "true" || value == "1") {
            return true
        }

        if (value == "false" || value == "0") {
            return false
        }

        return null
    }
}
