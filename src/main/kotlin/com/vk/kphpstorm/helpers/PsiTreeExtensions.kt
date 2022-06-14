package com.vk.kphpstorm.helpers

import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.parser.PhpElementTypes
import com.jetbrains.php.lang.parser.PhpPsiBuilder
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.impl.StringLiteralExpressionImpl

/**
 * Having a child inside doc comment (this), traverse psi tree up until the doc comment
 */
inline val PsiElement.parentDocComment: PhpDocComment?
    get() = PsiTreeUtil.getParentOfType(this, PhpDocComment::class.java)


/**
 * 'owner' of phpdoc — function/field/class, above which this phpdoc is written
 * Note! Owner is cached in PhpDocCommentImpl, updated only when editing the comment.
 */
fun PhpDocComment.getOwnerSmart(): PsiElement? =
        this.owner.takeIf {
            it is PhpClass                  // above 'class'
                    || it is Function       // above function or class method
                    || it is Field          // above class const / var / static field
            // if phpdoc is above any statement / other phpdoc — assume nothing
            // (this is typical when writing new code: at first phpdoc, than owner below,
            //  and at this moment owner is only kept in mind)
        }

fun convertArrayIndexPsiToStringIndexKey(indexPsi: PsiElement?): String? =
        when {
            indexPsi == null                               -> null
            indexPsi.elementType == PhpElementTypes.NUMBER -> indexPsi.text
            indexPsi is StringLiteralExpressionImpl        -> indexPsi.contents
            else                                           -> null
        }

fun setSelectionInEditor(editor: Editor, elementToSelect: PsiElement) {
    val offset = elementToSelect.textOffset
    editor.caretModel.caretsAndSelections = listOf(CaretState(
            editor.offsetToLogicalPosition(offset),
            editor.offsetToLogicalPosition(offset),
            editor.offsetToLogicalPosition(offset + elementToSelect.textLength)
    ))
}

/**
 * Example:
 *
 * ```
 *  builder.compare(token1, token2, ...etc)
 *  // equivalent to
 *  builder.compare(token1) || builder.compare(token2) || etc
 *  ```
 */
fun PhpPsiBuilder.compareAny(vararg tokens: IElementType): Boolean {
    return tokens.any { this.compare(it) }
}

/**
 * Example:
 *
 * ```
 *  builder.compareAndEatAny(token1, token2, ...etc)
 *  // equivalent to
 *  builder.compareAndEatAny(token1) || builder.compareAndEatAny(token2) || etc
 *  ```
 */
fun PhpPsiBuilder.compareAndEatAny(vararg tokens: IElementType): Boolean {
    return tokens.any { this.compareAndEat(it) }
}
