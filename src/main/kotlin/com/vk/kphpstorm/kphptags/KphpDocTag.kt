package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.PhpNamedElement

/**
 * All '@kphp-...' tags instances are objects extending this class.
 * (do not confuse it with elementType! elementType is used for psi/stubs interacting)
 *
 * When adding new tags (objects extending this class), also add them to list [ALL_KPHPDOC_TAGS]
 */
abstract class KphpDocTag(
    val nameWithAt: String,
) {
    val nameWithoutAt
        get() = nameWithAt.substring(1)

    abstract val description: String

    /**
     * Checks if usage @kphp-... in doc comment above the owner is correct
     * @param owner An element just below the doc comment
     */
    abstract fun isApplicableFor(owner: PsiElement): Boolean

    /**
     * Returns whether we need to show @kphp-... tag in autocompletion list inside doc comment
     */
    abstract fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean

    /**
     * Perform additional syntax check/highlighting of the tag body.
     * '@kphp-tag-name some text' — 'some text' is rhs
     */
    abstract fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder)

    /**
     * Returns whether duplicate of the same tag in one doc comment is not an error
     */
    open fun areDuplicatesAllowed(): Boolean {
        // by default, @kphp-... tag can meet only once in doc comment
        return false
    }

    /**
     * After pressing 'Enter' on suggestion, tag can auto-insert its argument (rhs).
     * Returned string can have '|' symbol — cursor position (if no — cursor will be placed after appended text).
     * If '|' is the first symbol, all tag value will be selected instead of placing the cursor before it.
     */
    open fun onAutoCompleted(docComment: PhpDocComment): String? {
        return null
    }

    fun existsInDocComment(docComment: PhpDocComment): Boolean {
        return findThisTagInDocComment(docComment) != null
    }

    fun existsInDocComment(docCommentOwner: PhpNamedElement): Boolean {
        val docComment = docCommentOwner.docComment ?: return false
        return existsInDocComment(docComment)
    }

    fun findThisTagInDocComment(docComment: PhpDocComment): PhpDocTag? {
        var tag: PhpDocTag? = null
        PhpDocUtil.consumeTagElementsByName(docComment, nameWithAt) { tag = it }
        return tag
    }

    fun findThisTagInDocComment(docCommentOwner: PhpNamedElement): PhpDocTag? {
        return PsiTreeUtil.findChildrenOfType(docCommentOwner.docComment, PhpDocTag::class.java)
            .firstOrNull { it.name == nameWithAt }
    }

    inline fun <reified T : PhpDocTag> findThisTagsInDocComment(docComment: PhpDocComment): List<T> {
        val tags = mutableListOf<T>()
        PhpDocUtil.consumeTagElementsByName(docComment, nameWithAt) {
            if (it is T) {
                tags.add(it)
            }
        }
        return tags
    }

    inline fun <reified T : PhpDocTag> findThisTagsInDocComment(docCommentOwner: PhpNamedElement): List<T> {
        return findThisTagsInDocComment(docCommentOwner.docComment ?: return listOf())
    }

    /**
     * Helper for use inside annotate() on syntax error
     */
    protected fun AnnotationHolder.errTag(docTag: PhpDocTag, errorMessage: String) {
        this.errElement(docTag, errorMessage)
    }

    /**
     * Helper for use inside annotate() on error
     */
    protected fun AnnotationHolder.errElement(element: PsiElement, errorMessage: String) {
        this.newAnnotation(HighlightSeverity.ERROR, errorMessage).range(element).create()
    }

    /**
     * Helper for use inside annotate() for simple tags, accepting no parameters
     */
    protected fun AnnotationHolder.warnTagIfAnyArgumentProvided(rhs: PsiElement?) {
        if (rhs != null)
            this.newAnnotation(HighlightSeverity.WARNING, "$nameWithAt does not accept any parameters").range(rhs).create()
    }
}

