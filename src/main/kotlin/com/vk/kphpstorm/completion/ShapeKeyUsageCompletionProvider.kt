package com.vk.kphpstorm.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression
import com.jetbrains.php.lang.psi.elements.ArrayIndex
import com.jetbrains.php.lang.psi.elements.PhpTypedElement

class ShapeKeyUsageCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val arrayIndex = PsiTreeUtil.getParentOfType(parameters.position, ArrayIndex::class.java) ?: return
        val lhs = (arrayIndex.parent as? ArrayAccessExpression)?.value as? PhpTypedElement ?: return
        val shapeItems = ShapeKeyInvocationCompletionProvider.detectPossibleKeysOfShape(lhs.type) ?: return

        for (item in shapeItems)
            result.addElement(LookupElementBuilder.create(item.keyName).withTypeText(item.type.toString()).withInsertHandler(ArrayKeyInsertHandler))

        // PhpStorm also tries to suggest keys based on usage (not on type, of course)
        // and they duplicate with our intentions if once used
        result.runRemainingContributors(parameters) { _ ->
            // so, filter out everything PhpStorm tries to suggest
        }
    }

    /**
     * After selecting a shape key inside ['...'], place caret after ']
     */
    private object ArrayKeyInsertHandler : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, element: LookupElement) {
            val caretOffset = context.editor.caretModel.offset  // before closing '
            context.editor.caretModel.moveToOffset(caretOffset + 2)
        }
    }
}
