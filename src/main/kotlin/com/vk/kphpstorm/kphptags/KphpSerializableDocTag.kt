package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.PhpClass

object KphpSerializableDocTag : KphpDocTag("@kphp-serializable") {
    override val description: String
        get() = "Classes with this tag can be stored in binary format: instance_serialize() and instance_deserialize() — for storing in memcache, for example. All fields must have @kphp-serialized-field, this makes versioning work."

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
