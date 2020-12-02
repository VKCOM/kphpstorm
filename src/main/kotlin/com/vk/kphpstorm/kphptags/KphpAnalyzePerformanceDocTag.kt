package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagElementType

object KphpAnalyzePerformanceDocTag : KphpDocTag("@kphp-analyze-performance") {
    override val description: String
        get() = "Generates a report with potential optimizations, such as computing a constant value outside a loop or implicit arrays casting."

    override val elementType: KphpDocTagElementType
        get() = KphpDocElementTypes.kphpDocTagWarnPerformance

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return owner is Function && !KphpWarnPerformanceDocTag.existsInDocComment(docComment)
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (KphpWarnPerformanceDocTag.existsInDocComment(docTag.parentDocComment ?: return))
            holder.errTag(docTag, "Both warn and analyze annotations exist")
        else
            KphpWarnPerformanceDocTag.annotate(docTag, rhs, holder)
    }

    override fun onAutoCompleted(docComment: PhpDocComment): String? {
        return KphpWarnPerformanceDocTag.onAutoCompleted(docComment)
    }
}
