package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl

/**
 * Inside '@kphp-warn-performance all !implicit-array-cast' — there are two elements of this impl
 * Same for '@kphp-analyze-performance'
 * @see KphpDocTagWarnPerformanceElementType.getTagParser
 */
class KphpDocWarnPerformanceItemPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node) {
    companion object {
        val elementType = PhpDocElementType("phpdocWarnPerformanceItem")
    }

    override fun getName(): String = text.let { if (it.first() == '!') it.substring(1) else it }

    fun isNegation() = text.startsWith('!')
}
