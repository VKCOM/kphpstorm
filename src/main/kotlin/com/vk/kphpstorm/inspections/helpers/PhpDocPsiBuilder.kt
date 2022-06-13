package com.vk.kphpstorm.inspections.helpers

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.kphptags.KphpSerializedFieldDocTag

/**
 * Builds and moves around phpdoc psi elements
 */
object PhpDocPsiBuilder {

    fun addKphpSerializedToField(field: Field, project: Project) {
        val docComment = field.docComment ?: createDocComment(project, field)
        val serializedFieldIndex = KphpSerializedFieldDocTag.onAutoCompleted(docComment) ?: ""
        docComment.transformToMultiline(project).addTag(project, KphpSerializedFieldDocTag.nameWithAt, serializedFieldIndex)
    }

    fun removeTagFromDocComment(docComment: PhpDocComment, nameWithAt: String) {
        for (docTag in docComment.getTagElementsByName(nameWithAt))
            docComment.removeTag(docTag)
    }

    fun removeTagFromDocComment(docTag: PhpDocTag) {
        val docComment = docTag.parentDocComment ?: return
        docComment.removeTag(docTag)
    }

    fun addDocTagToParameter(parameter: Parameter, function: Function, project: Project): PhpDocTag {
        val docComment = function.docComment ?: createDocComment(project, function)
        var prevParameterTag: PhpDocTag? = null
        var prevParameter = parameter.prevSibling
        while (prevParameter != null && prevParameterTag == null) {
            if (prevParameter is Parameter)
                prevParameterTag = docComment.paramTags.firstOrNull { it.varName == (prevParameter as Parameter).name }
            prevParameter = prevParameter.prevSibling
        }
        return docComment.addTag(project, "@param", "type $${parameter.name}", prevParameterTag)
    }

    fun addDocTagToReturn(function: Function, project: Project): PhpDocTag {
        val docComment = function.docComment ?: createDocComment(project, function)
        var lastTag: PhpDocTag? = null
        var child = docComment.firstPsiChild
        while (child != null) {
            if (child is PhpDocTag)
                lastTag = child
            child = child.nextPsiSibling
        }
        return docComment.addTag(project, "@return", "type", lastTag)
    }

    fun addDocTagToField(field: Field, project: Project): PhpDocTag {
        if (field.docComment == null)
            return createDocComment(project, field, " @var type ").varTag!!
        return field.docComment!!.addTag(project, "@var", "type")
    }


    /**
     * Create empty phpdoc and insert it before function/class/field
     */
    fun createDocComment(project: Project, element: PhpNamedElement, contents: String = "\n"): PhpDocComment {
        val docComment = PhpPsiElementFactory.createFromText(project, PhpDocElementTypes.DOC_COMMENT, "/**$contents*/")
        val insLevel = if (element is Field) element.parent else element
        return insLevel.parent.addBefore(docComment, insLevel) as PhpDocComment
    }

    /**
     * Having '/** @var int */', transform to '/** \n * @var int \n */'
     */
    fun PhpDocComment.transformToMultiline(project: Project): PhpDocComment {
        val firstTag = children.find { it is PhpDocTag }
        var item = PsiTreeUtil.skipWhitespacesBackward(firstTag)

        if (firstTag != null && item != null && item.elementType != PhpTokenTypes.DOC_LEADING_ASTERISK) {
            val isMultiLine = text.contains('\n')
            if (isMultiLine)
                return this

            var prependingText = ""
            while (item != null && item.elementType != PhpTokenTypes.DOC_COMMENT_START) {
                prependingText = item.text + prependingText
                item = item.prevSibling
            }

            val fullDocText = "/**$prependingText\n* ${firstTag.text} */"
            return replace(PhpPsiElementFactory.createFromText(project, PhpDocElementTypes.DOC_COMMENT, fullDocText)) as PhpDocComment
        }
        return this
    }

    fun PhpDocComment.addTag(project: Project, nameWithAt: String, tagValue: String = "", afterTag: PhpDocTag? = null): PhpDocTag {
        val firstTagStart = PsiTreeUtil.skipWhitespacesBackward(children.find { it is PhpDocTag })
        val firstTagAnchor = when (firstTagStart?.elementType) {
            PhpTokenTypes.DOC_LEADING_ASTERISK -> firstTagStart
            else                               -> lastChild
        }

        val leadingAsterisk = PhpPsiElementFactory.createFromText(project, PhpDocTokenTypes.DOC_LEADING_ASTERISK, "/**\n* */")
        val eAsterisk =
                if (afterTag != null) addAfter(leadingAsterisk, afterTag)
                else addBefore(leadingAsterisk, firstTagAnchor)

        val docTagContent = if (tagValue.isEmpty()) nameWithAt else "$nameWithAt $tagValue"
        val phpDocTag = PhpPsiElementFactory.createPhpDocTag(project, docTagContent)
        return addAfter(phpDocTag, eAsterisk) as PhpDocTag
    }

    /**
     * Removes @tag from doc comment.
     * If it contains nothing but '@tag', removes doc comment itself.
     */
    private fun PhpDocComment.removeTag(docTag: PhpDocTag) {
        val asterisk = PsiTreeUtil.skipWhitespacesBackward(docTag)?.takeIf { it.elementType == PhpTokenTypes.DOC_LEADING_ASTERISK }
        docTag.delete()
        asterisk?.delete()

        var child = firstChild.nextSibling
        while (child.elementType == PhpTokenTypes.DOC_LEADING_ASTERISK || child is PsiWhiteSpace)
            child = child.nextSibling
        if (child.elementType == PhpTokenTypes.DOC_COMMENT_END)
            delete()
    }
}
