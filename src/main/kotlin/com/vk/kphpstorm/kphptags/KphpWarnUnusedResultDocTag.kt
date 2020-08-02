package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function

object KphpWarnUnusedResultDocTag : KphpDocTag("@kphp-warn-unused-result") {
    override val description: String
        get() = "This tag is for non-void functions. If an invocation doesn't use the result (not saved to variable, not passed as argument), you get compilation error."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return owner is Function && (docComment.returnTag != null || owner.typeDeclaration != null)
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        holder.warnTagIfAnyArgumentProvided(rhs)
    }
}
