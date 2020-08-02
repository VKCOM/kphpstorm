package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.PhpClass

object KphpImmutableClassDocTag : KphpDocTag("@kphp-immutable-class") {
    override val description: String
        get() = "Fields of immutable class are deeply constant (can be set only in constructor). All nested instances must be also immutable. Such instances can be stored in instance cache."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is PhpClass
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        // do not suggest if already in class (nested classes not supported)
        if (owner == null)
            return PsiTreeUtil.getParentOfType(docComment, PhpClass::class.java) == null
        return owner is PhpClass
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        holder.warnTagIfAnyArgumentProvided(rhs)
    }
}
