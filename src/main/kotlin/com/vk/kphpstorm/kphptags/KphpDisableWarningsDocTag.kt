package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function

object KphpDisableWarningsDocTag : KphpDocTag("@kphp-disable-warnings") {
    override val description: String
        get() = "Very uncommon tag to suppress compilation warning of a specified function."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        // this tag is very rare; IDE recognizes it, but does not auto-suggest
        return false
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null)
            holder.errTag(docTag, "Specify warning names to suppress")
    }
}
