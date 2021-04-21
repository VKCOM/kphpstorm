package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function

object KphpThrowsDocTag : KphpDocTag("@kphp-throws") {
    override val description: String
        get() = "If a function turns out to throw exceptions other than specified, you will get a compilation error."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return owner == null || owner is Function
    }

    override fun onAutoCompleted(docComment: PhpDocComment): String? {
        return "\\Exception"
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null)
            holder.errTag(docTag, "Specify a list of exception classes, separated by space")
    }
}
