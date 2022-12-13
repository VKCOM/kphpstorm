package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function

object KphpInferDocTag : KphpDocTag("@kphp-infer") {
    override val description: String
        get() = "Previously was used to make KPHP parse phpdoc. Now deprecated, as all functions are typed."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return false
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        // don't do anything here:
        // @kphp-infer is deprecated, and KphpDocInspection highlights it with removal quick fix
    }

    fun isKphpInferCast(tag: PhpDocTag): Boolean {
        return tag.name == nameWithAt && PhpDocUtil.getTagValue(tag, false).trim() == "cast"
    }
}
