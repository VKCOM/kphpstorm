package com.vk.kphpstorm.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.jetbrains.php.completion.PhpCompletionContributor
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.highlighter.PhpHighlightingData
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.FunctionImpl
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.helpers.KPHP_NATIVE_FUNCTIONS
import com.vk.kphpstorm.kphptags.ALL_KPHPDOC_TAGS
import com.vk.kphpstorm.kphptags.psi.KphpDocTagImpl
import java.util.*

/**
 * Annotator ~ highlighter.
 * It can apply highlighting rules and errors.
 * It works only with ready psi, mostly invoked on a currently opened file (by internals of IDEA).
 *
 * Here we do the following:
 * * highlight phpdoc tags (@kphp-... phpdoc tags differ from others)
 * * highlight function calls (differ php native / kphp native / user written)
 * * highlight classes and interfaces separately (miss this feature in PhpStorm)
 * * check correctness of @kphp-... phpdoc tags
 *
 * Caution!
 * We want 'tuple' to be highlighted as keyword, not as a function call.
 * But PhpStorm default annotator thinks it's just a function.
 * If we just apply text attrubutes for 'tuple', they will be messed with default highlighing (race condition).
 * There is only one way to avoid this:
 * 1) Provide highlighting not only to 'tuple', but for all function calls
 * 2) Manually disable default highlighting settings in Color Scheme > PHP > Function call
 *    (uncheck all checkboxes)
 * 3) Re-configure our color settings in Color Scheme > KPHP:
 *    (configure regular function calls)
 * Same for phpdoc tags. We want '@kphp-...' to be highlighted differently, that's why here
 * we provide annotation all phpdoc tags, and a user at first must uncheck all default highlighting in settings.
 */
class KphpStormAnnotator : Annotator {
    // these function calls are highlighted as keywords
    private val KPHP_KEYWORDS: SortedSet<String> = sortedSetOf(
            "tuple",
            "shape",
            "fork",
            "not_null",
            "not_false"
    )

    /**
     * This main function is called for every psi element
     */
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is PhpDocTagImpl         -> onPhpdocTag(element, holder)
            is FunctionReferenceImpl -> onFuncCall(element, holder)
            is PhpDocTypeImpl        -> onTypeInsidePhpdocTag(element, holder)
            is ClassReferenceImpl    -> onClassReference(element, holder)
            is PsiComment            -> onComment(element, holder)
            is FunctionImpl          -> onFunctionDeclaration(element, holder)
        }
    }

    /**
     * Any block comment or line comment
     */
    private fun onComment(element: PsiComment, holder: AnnotationHolder) {
        if (element.elementType == PhpTokenTypes.LINE_COMMENT) {
            val text = element.text
            if (text.startsWith("#ifndef") || text.startsWith("#endif")) {
                // just bold, no customization
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .enforcedTextAttributes(TextAttributes().apply { fontType = 1 }).range(element).create()
            }
        }
    }

    /**
     * Any phpdoc tag: line '@tag ...', or inline tag like {@see ...}
     */
    private fun onPhpdocTag(element: PhpDocTagImpl, holder: AnnotationHolder) {
        val tagNameE = element.firstChild ?: return
        val isKphpDocTag = element is KphpDocTagImpl

        // for @kphp-... doc tags call annotate() to highlight custom per-tag errors
        // its psi tree: tagNameE + whitespace + PhpPsiElementImpl (contains everything till tag end, can be empty)
        // if it is not empty, treat it as argument (rhs)
        if (isKphpDocTag) {
            holder.textAttributes(tagNameE, KphpHighlightingData.PHPDOC_TAG_KPHP)
            val nameWithAt = tagNameE.text
            val rhs = PsiTreeUtil.skipWhitespacesForward(tagNameE)?.takeIf { it.firstChild != null }
            ALL_KPHPDOC_TAGS.find { it.nameWithAt == nameWithAt }?.apply {
                annotate(element, rhs, holder)
            }
        }
        else
            holder.textAttributes(tagNameE, KphpHighlightingData.PHPDOC_TAG_REGULAR)

        // inside @var / @param / @return highlight PhpDocType (it can be either after tag of after var name)
        var child = element.firstPsiChild
        if (child !is PhpDocType)
            child = child?.nextPsiSibling
        if (child is PhpDocType)
            holder.textAttributes(child, KphpHighlightingData.PHPDOC_TYPE_INSIDE)
    }

    /**
     * Type inside phpdoc tag (in @var/@param/@return); types are nested: expression, array, tuple, etc
     */
    @Suppress("UNUSED_PARAMETER")
    private fun onTypeInsidePhpdocTag(element: PhpDocTypeImpl, holder: AnnotationHolder) {
        // for future: highlight primitives and classes, template args and shape keys
    }

    /**
     * Any func call (plain function, not method, cause static/instance methods are MethodReferenceImpl)
     */
    private fun onFuncCall(element: FunctionReferenceImpl, holder: AnnotationHolder) {
        val funcName = element.name
        val funcNameE = element.firstChild ?: return

        holder.textAttributes(funcNameE, when {
            KPHP_KEYWORDS.contains(funcName)                                     -> PhpHighlightingData.KEYWORD
            KPHP_NATIVE_FUNCTIONS.contains(funcName)                             -> KphpHighlightingData.FUNC_CALL_KPHP_NATIVE
            PhpCompletionContributor.PHP_PREDEFINED_FUNCTIONS.contains(funcName) -> PhpHighlightingData.PREDEFINED_SYMBOL
            else                                                                 -> KphpHighlightingData.FUNC_CALL_REGULAR
        })
    }

    /**
     * Any class/interface/trait name
     */
    private fun onClassReference(element: ClassReferenceImpl, holder: AnnotationHolder) {
        // primitive type hints 'string' etc are also ClassReference, but we are not interested in them
        // (they are highlighted as PRIMITIVE_TYPE_HINT natively, I didn't change this)
        val text = element.text
        if (PhpType.isPrimitiveType(text) || text == "self" || text == "static" || text == "parent")
            return

        if (element.parent is NewExpression) {
            // element.resolve() can be either PhpClass or Method __construct
            holder.textAttributes(element, PhpHighlightingData.CLASS)
        }
        else {
            val refTo = element.resolve() as? PhpClass
            if (refTo != null && !refTo.isTrait)
                holder.textAttributes(element, if (refTo.isInterface) PhpHighlightingData.INTERFACE else PhpHighlightingData.CLASS)
        }
    }

    /**
     * Special highlight for php polyfills of kphp-builtin functions
     */
    private fun onFunctionDeclaration(element: Function, holder: AnnotationHolder) {
        if (element !is Method && KPHP_NATIVE_FUNCTIONS.contains(element.name)) {
            // just bold and italics, no customization
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .enforcedTextAttributes(TextAttributes().apply { fontType = 3 })
                    .range(element.nameIdentifier!!).create()
        }
    }

    private fun AnnotationHolder.textAttributes(element: PsiElement, textAttributes: TextAttributesKey) {
        newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(textAttributes).create()
    }
}
