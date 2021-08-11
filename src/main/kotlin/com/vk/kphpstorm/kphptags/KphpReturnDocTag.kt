package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function

object KphpReturnDocTag : KphpDocTag("@kphp-return") {
    override val description: String
        get() = "[KPHP] Can be used if @kphp-template was specified, to provide information of returning class/fields based on template argument."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function && KphpTemplateDocTag.existsInDocComment(owner)
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return KphpTemplateDocTag.existsInDocComment(docComment)
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null)
            holder.errTag(docTag, "[KPHP] Specify returning class/fields based on template argument.")
    }
}
