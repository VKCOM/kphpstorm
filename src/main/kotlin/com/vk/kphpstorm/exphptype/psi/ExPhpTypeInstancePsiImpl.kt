package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * 'A', 'asdf\Instance', '\VK\Memcache' — instances (not primitives!) — psi is just PhpDocType
 * PhpType is resolving relative to absolute path, like '\A'
 */
class ExPhpTypeInstancePsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeInstance")
    }

    override fun getType(): PhpType {
        // for "future" don't invoke getType(), because it will be treated as relative class name in namespace
        return if (isKphpBuiltinClass()) PhpType().add(text) else getType(this, text)
    }

    fun isKphpBuiltinClass() = text.let {
        it == "future" || it == "future_queue" || it == "\\RpcConnection"
    }
}
