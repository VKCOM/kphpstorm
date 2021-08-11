package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Field

object KphpSerializedFloat32DocTag : KphpDocTag("@kphp-serialized-float32") {
    override val description: String
        get() = "[KPHP] For a serialized field, specifies that all floats inside it are serialized as 32-bit, not 64-bit."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Field && KphpSerializedFieldDocTag.existsInDocComment(owner)
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return KphpSerializedFieldDocTag.existsInDocComment(docComment)
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        holder.warnTagIfAnyArgumentProvided(rhs)
    }
}
