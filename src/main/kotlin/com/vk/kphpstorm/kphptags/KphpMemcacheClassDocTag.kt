package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.PhpClass

object KphpMemcacheClassDocTag : KphpDocTag("@kphp-memcache-class") {
    override val description: String
        get() = "[KPHP] Global \$MC, \$MC_Ads and others are assumed to be instances of this class. Can appear only once in a project."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is PhpClass
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        // never need to use this doc tag in regular backend code
        return false
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {}
}




