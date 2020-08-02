package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function

object KphpTemplateDocTag : KphpDocTag("@kphp-template") {
    override val description: String
        get() = "Used above functions to implement duck typing: such functions are 'overloaded' and can accept variuos OOP-incompatible instances."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        // this tag is very rare; IDE recognizes it, but does not auto-suggest
        return false
    }

    override fun areDuplicatesAllowed(): Boolean {
        // @kphp-template can meet several times, for each parameter
        return true
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null)
            holder.errTag(docTag, "Expected: [T] \$arg [,\$arg2,...]")
        // do not complicate rhs verification logic, because this tag is very rare
    }
}
