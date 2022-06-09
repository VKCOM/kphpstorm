package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.highlighter.PhpHighlightingData
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.GenericUtil
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.isStringableStringUnion
import com.vk.kphpstorm.highlighting.KphpHighlightingData
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagElementType
import com.vk.kphpstorm.kphptags.psi.KphpDocTagGenericPsiImpl

object KphpGenericDocTag : KphpDocTag("@kphp-generic") {
    override val description: String
        get() = "Describes generic types for a function and makes it generic"

    override val elementType: KphpDocTagElementType
        get() = KphpDocElementTypes.kphpDocTagGeneric

    override fun isApplicableFor(owner: PsiElement) = owner is Function || owner is PhpClass

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?) = true

    override fun areDuplicatesAllowed() = false

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
            val genericArguments = docTag.getFullGenericParameters()
            val names = mutableSetOf<String>()
            genericArguments.forEach { decl ->
                checkExtendsType(decl.extendsType, holder, docTag)

                if (names.contains(decl.name)) {
                    holder.errTag(docTag, "Duplicate generic type ${decl.name} in declaration")
                }
                names.add(decl.name)
            }

            val parentClass = docTag.parentOfType<PhpClass>()
            parentClass?.genericNames()?.forEach { decl ->
                if (names.contains(decl.name)) {
                    holder.errTag(docTag, "Duplicate generic type ${decl.name} (first seen in class declaration)")
                }
            }
        }

        if (docTag is KphpDocTagGenericPsiImpl) {
            docTag.getParametersPsi().forEach { psi ->
                if (psi.namePsi != null) {
                    holder.highlight(psi.namePsi, KphpHighlightingData.PHPDOC_TYPE_INSIDE)
                }
                if (psi.extendsTypePsi != null) {
                    holder.highlight(psi.extendsTypePsi!!, PhpHighlightingData.DOC_COMMENT)
                }
                if (psi.defaultTypePsi != null) {
                    holder.highlight(psi.defaultTypePsi!!, PhpHighlightingData.DOC_COMMENT)
                }
            }
        }
    }

    private fun checkExtendsType(
        extendsType: ExPhpType?,
        holder: AnnotationHolder,
        docTag: PhpDocTag
    ) {
        if (extendsType == null) {
            return
        }

        if (extendsType is ExPhpTypePrimitive || extendsType is ExPhpTypeCallable || extendsType is ExPhpTypeInstance) {
            return
        }

        if (extendsType is ExPhpTypePipe) {
            if (extendsType.isStringableStringUnion()) {
                return
            }

            val allInstance = extendsType.items.all {
                it is ExPhpTypeInstance || it is ExPhpTypeTplInstantiation || it is ExPhpTypeGenericsT
            }
            val allPrimitives = extendsType.items.all { it is ExPhpTypePrimitive }

            if (!allInstance && !allPrimitives) {
                holder.errTag(
                    docTag,
                    "Union type can contain either only instances or only primitives (except '\\Stringable|string')"
                )
                return
            }

            return
        }

        holder.errTag(docTag, "Type '$extendsType' is not allowed here")
    }
}
