package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function

object KphpNoReturnDocTag : KphpDocTag("@kphp-no-return") {
    override val description: String
        get() = "Indicates, that this function never returns (always calls exit()). While building control flow graph, kphp treats all code after such functions invocations as unaccessible, does not warn on missing break, etc."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        // this tag is very rare; IDE recognizes it, but does not auto-suggest
        return false
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        holder.warnTagIfAnyArgumentProvided(rhs)
    }
}
