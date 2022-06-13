package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.highlighter.PhpHighlightingData
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeTplInstantiationPsiImpl
import com.vk.kphpstorm.generics.GenericUtil.genericNonDefaultNames
import com.vk.kphpstorm.generics.GenericUtil.genericParents
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocInheritParameterDeclPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagElementType
import com.vk.kphpstorm.kphptags.psi.KphpDocTagInheritPsiImpl

object KphpInheritDocTag : KphpDocTag("@kphp-inherit") {
    override val description: String
        get() = "Describes generic inherit for a class"

    override val elementType: KphpDocTagElementType
        get() = KphpDocElementTypes.kphpDocTagInherit

    override fun isApplicableFor(owner: PsiElement) = owner is PhpClass

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?) = true

    override fun areDuplicatesAllowed() = false

    override fun onAutoCompleted(docComment: PhpDocComment): String {
        val klass = docComment.owner as? PhpClass ?: return ""
        val parentsList = klass.genericParents()

        val parentsString = parentsList.joinToString(", ") {
            val genericTs = it.genericNonDefaultNames().joinToString(", ") { "T" }
            it.name + "<$genericTs>"
        }

        return "$parentsString|"
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null) {
            holder.errTag(docTag, "Expected: ExtendsClass<Type>[, ExtendsClass2<Type>, ImplementsClass<Type>]")
            return
        }

        if (docTag is KphpDocTagInheritPsiImpl) {
            holder.highlight(docTag, PhpHighlightingData.DOC_COMMENT)
        }

        val parameters = PsiTreeUtil.findChildrenOfType(rhs.parent, KphpDocInheritParameterDeclPsiImpl::class.java)
        parameters.forEach {
            it as KphpDocInheritParameterDeclPsiImpl
            val child = it.type()
            if (child !is ExPhpTypeTplInstantiationPsiImpl && child !is ExPhpTypeInstancePsiImpl) {
                holder.error(it.firstChild, "Expected ClassName<Type>, got ${it.firstChild.text}")
            }
        }
    }
}
