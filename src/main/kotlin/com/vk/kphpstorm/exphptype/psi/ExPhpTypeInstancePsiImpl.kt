package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.elements.PhpClassFieldsList
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.generics.GenericUtil

/**
 * 'A', 'asdf\Instance', '\VK\Memcache' — instances (not primitives!) — psi is just PhpDocType
 * PhpType is resolving relative to absolute path, like '\A'
 */
class ExPhpTypeInstancePsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeInstance")
    }

    override fun getType(): PhpType {
        val text = text

        // for "future" don't invoke getType(), because it will be treated as relative class name in namespace
        if (isKphpBuiltinClass()) {
            PhpType().add(text)
        }

        if (isGenericT()) {
            return PhpType().add("%$text")
        }

        return getType(this, text)
    }

    fun isGenericT(): Boolean {
        val isGenericT = GenericUtil.nameIsGeneric(this, text)

        val phpDoc = parent.parent ?: return isGenericT
        val element = PsiTreeUtil.skipSiblingsForward(phpDoc, PsiWhiteSpace::class.java)
        if (element is PhpClassFieldsList) {
            if (element.modifierList?.hasStatic() == true) {
                // For static fields is always not genericT
                return false
            }
        }

        return isGenericT
    }

    fun isKphpBuiltinClass() = text.let {
        it == "future" || it == "future_queue" || it == "\\RpcConnection"
    }
}
