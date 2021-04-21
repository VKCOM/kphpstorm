package com.vk.kphpstorm.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.CaretState
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.PhpFile
import com.vk.kphpstorm.helpers.getOwnerSmart
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.kphptags.ALL_KPHPDOC_TAGS

/**
 * Completing phpdoc tag names: '@kphp-...'
 * @see com.vk.kphpstorm.kphptags.KphpDocTag.needsAutoCompleteOnTyping
 * Also handling insertion (for example, '@kphp-serialized-field' inserts the next numeric index)
 * @see com.vk.kphpstorm.kphptags.KphpDocTag.onAutoCompleted
 */
class KphpDocTagNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val docComment = parameters.position.parentDocComment ?: return
        val owner = docComment.getOwnerSmart()

        for (tag in ALL_KPHPDOC_TAGS)
            if (tag.needsAutoCompleteOnTyping(docComment, owner) && (tag.areDuplicatesAllowed() || !tag.existsInDocComment(docComment))) {
                result.addElement(LookupElementBuilder.create(tag.nameWithoutAt).withInsertHandler(KphpDocTagNameInsertHandler))
            }

        // Since 2020.2, PhpStorm suggest any tags occured at least once in project
        // (so, it suggests @kphp-serializable above everything and so on)
        // disable this "feature": the plugin knows context better
        result.runRemainingContributors(parameters) { t ->
            val isKphpTag = t.lookupElement.lookupString.startsWith("kphp")
            if (!isKphpTag)
                result.addElement(t.lookupElement)
        }
    }

    private object KphpDocTagNameInsertHandler : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, element: LookupElement) {
            val nameWithAt = "@" + element.lookupString
            val kphpDocImpl = ALL_KPHPDOC_TAGS.find { it.nameWithAt == nameWithAt } ?: return
            val file = context.file as PhpFile
            val caretOffset = context.editor.caretModel.offset
            val docComment = file.findElementAt(caretOffset)?.parentDocComment ?: return

            var appendText = kphpDocImpl.onAutoCompleted(docComment) ?: return
            val cursorPos = appendText.indexOf('|')
            if (cursorPos != -1) {
                appendText = appendText.substring(0, cursorPos) + appendText.substring(cursorPos + 1)
            }

            context.document.insertString(caretOffset, " $appendText")
            when {
                // move cursor to the end
                cursorPos == -1 -> context.editor.caretModel.moveToOffset(caretOffset + 1 + appendText.length)
                // move cursor to the | position
                cursorPos > 0   -> context.editor.caretModel.moveToOffset(caretOffset + 1 + cursorPos)
                // select all tag value
                cursorPos == 0  -> context.editor.caretModel.caretsAndSelections = listOf(CaretState(
                        context.editor.offsetToLogicalPosition(caretOffset + 1),
                        context.editor.offsetToLogicalPosition(caretOffset + 1),
                        context.editor.offsetToLogicalPosition(caretOffset + 1 + appendText.length)
                ))
            }
        }
    }
}
