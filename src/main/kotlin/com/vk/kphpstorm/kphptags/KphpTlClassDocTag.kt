package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.PhpClass

object KphpTlClassDocTag : KphpDocTag("@kphp-tl-class") {
    override val description: String
        get() = "[KPHP] Indicates that this class is auto-generated from tl scheme. Kphp will generate storing/fetching algorithms on compilation. Used only in auto-generated code."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is PhpClass
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        // this tag is used only in auto-generated code
        return false
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {}
}
