package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.generics.GenericUtil
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagElementType
import com.vk.kphpstorm.kphptags.psi.KphpDocTagGenericPsiImpl

object KphpGenericDocTag : KphpDocTag("@kphp-generic") {
    override val description: String
        get() = "Describes generic types for a function and makes it generic"

    override val elementType: KphpDocTagElementType
        get() = KphpDocElementTypes.kphpDocTagGeneric

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function || owner is PhpClass
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return true
    }

    override fun areDuplicatesAllowed(): Boolean {
        return false
    }

    override fun onAutoCompleted(docComment: PhpDocComment): String {
        val parentClass = docComment.parentOfType<PhpClass>()
        return "|" + GenericUtil.generateUniqueGenericName(parentClass?.genericNames())
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null) {
            holder.errTag(docTag, "Expected: T[: ExtendsClass] [, T1[: ExtendsClass] [= DefaultType], ...]")
            return
        }

        if (docTag is KphpDocTagGenericPsiImpl) {
            val names = mutableSetOf<String>()
            docTag.getGenericArguments().forEach { name ->
                if (names.contains(name)) {
                    holder.errTag(docTag, "Duplicate generic type $name in declaration")
                }
                names.add(name)
            }

            val parentClass = docTag.parentOfType<PhpClass>()
            parentClass?.genericNames()?.forEach { decl ->
                if (names.contains(decl.name)) {
                    holder.errTag(docTag, "Duplicate generic type ${decl.name} (first seen in class declaration)")
                }
            }
        }
    }
}
