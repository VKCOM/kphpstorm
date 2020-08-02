package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagElementType

object KphpTemplateClassDocTag : KphpDocTag("@kphp-template-class") {
    override val description: String
        get() = "Experiments for future, try to implement concept of template classes in php"

    override val elementType: KphpDocTagElementType
        get() = KphpDocElementTypes.kphpDocTagTemplateClass

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is PhpClass
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        // this is experimental for IDE only, not supported in kphp for now
        return false
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        // rhs is the first template tag name, other can be accessed with nextSibling etc
        if (rhs == null)
            holder.errTag(docTag, "Template arguments not specified")
    }
}
