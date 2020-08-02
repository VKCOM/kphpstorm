package com.vk.kphpstorm.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeNullable
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.ExPhpTypeShape
import com.vk.kphpstorm.helpers.toExPhpType

/**
 * f(shape(['...'])) — suggest possible shape keys when invoking f() with shape argument
 */
class ShapeKeyInvocationCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val array = PsiTreeUtil.getParentOfType(parameters.position, ArrayCreationExpression::class.java) ?: return
        val shapeCall = PsiTreeUtil.getParentOfType(array, FunctionReference::class.java)
                ?.takeIf { it.name == "shape" } ?: return
        val funcCall = PsiTreeUtil.getParentOfType(shapeCall, FunctionReference::class.java) ?: return
        val func = funcCall.multiResolve(false).firstOrNull()?.element as? Function ?: return
        val funcParameters = func.parameters
        val argIndex = funcCall.parameters.indexOf(shapeCall)
                .takeIf { it >= 0 && it < funcParameters.size } ?: return
        val paramType = funcParameters[argIndex].type.takeIf { !it.isEmpty } ?: return
        val shapeItems = detectPossibleKeysOfShape(paramType) ?: return
        val usedKeys = array.hashElements.mapNotNull { it.key?.text?.trim('\'', '\"') }

        for (item in shapeItems)
            if (!usedKeys.contains(item.keyName))
                result.addElement(LookupElementBuilder.create(item.keyName).withTypeText(item.type.toString()).withInsertHandler(ArrayKeyInsertHandler))
    }

    /**
     * After selecting a shape key (passed as array key), insert => and place caret after.
     */
    private object ArrayKeyInsertHandler : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, element: LookupElement) {
            val caretOffset = context.editor.caretModel.offset  // before closing '
            context.document.insertString(caretOffset + 1, " => ")
            context.editor.caretModel.moveToOffset(caretOffset + 5)
        }
    }

    companion object {
        /**
         * Having PhpType e.g. "shape(...)|null" — get items of that shape
         */
        fun detectPossibleKeysOfShape(type: PhpType): List<ExPhpTypeShape.ShapeItem>? {
            val parsed = type.toExPhpType()
            val shapeInType = when (parsed) {
                is ExPhpTypePipe     -> parsed.items.firstOrNull { it is ExPhpTypeShape }
                is ExPhpTypeNullable -> parsed.inner
                else                 -> parsed
            } as? ExPhpTypeShape ?: return null

            return shapeInType.items
        }
    }
}
