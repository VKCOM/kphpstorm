package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass

object KphpConstDocTag : KphpDocTag("@kphp-const") {
    override val description: String
        get() = "Class fields marked @kphp-const can be set only in constructor. Constantness is not deep: array elements and nested instance properties can still be modified, so constant is the field itself."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Field && !owner.isConstant && owner.modifier.isDynamic
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        PsiTreeUtil.getParentOfType(docComment, PhpClass::class.java) ?: return false
        return owner == null || isApplicableFor(owner)
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        holder.warnTagIfAnyArgumentProvided(rhs)
    }
}
