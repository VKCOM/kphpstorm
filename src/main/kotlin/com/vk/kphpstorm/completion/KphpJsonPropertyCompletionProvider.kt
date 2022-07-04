package com.vk.kphpstorm.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.KphpJsonTag

class KphpJsonPropertyCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val owner = position.parentDocComment?.owner as? PhpTypedElement ?: return

        if (owner is Field && owner.modifier.isStatic) {
            return
        }

        val elementBeforeCursor = PsiTreeUtil.skipWhitespacesBackward(position)
        when (elementBeforeCursor?.text?.trim()) {
            "=" -> {
                val elementName = PsiTreeUtil.skipWhitespacesBackward(elementBeforeCursor)?.text ?: return

                val property = KphpJsonTag.properties.firstOrNull { it.name == elementName } ?: return
                property.allowValues?.forEach { value ->
                    result.addElement(LookupElementBuilder.create(value))
                }

                if (property.booleanValue) {
                    result.addElement(LookupElementBuilder.create("true"))
                    result.addElement(LookupElementBuilder.create("false"))
                }
            }

            // if no text before cursor
            null -> {
                val isField = owner is Field
                val isClass = owner is PhpClass

                val forElement = LookupElementBuilder.create("for").appendTailText(" ViewName", true)
                    .withInsertHandler(KphpDocJsonForEncoderInsertHandler)
                result.addElement(forElement)

                for (property in KphpJsonTag.properties) {
                    if (property.allowField != isField && property.allowClass != isClass) {
                        continue
                    }

                    if (property.ifType != null) {
                        if (!property.ifType.first.invoke(owner.type.toExPhpType())) {
                            continue
                        }
                    }

                    var element = LookupElementBuilder.create(property.name)
                    val allowValues = property.allowValues
                    if (allowValues != null) {
                        element = element.appendTailText("=", true).withInsertHandler(KphpDocJsonPropertyInsertHandler)

                        if (allowValues.isNotEmpty()) {
                            element = element.withTypeText(allowValues.joinToString("|"))
                        }
                    }

                    result.addElement(element)
                }
            }
        }
    }

    private object KphpDocJsonPropertyInsertHandler : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, element: LookupElement) {
            val editor = context.editor
            val caretOffset = editor.caretModel.offset

            context.document.insertString(caretOffset, "=")
            editor.caretModel.moveToOffset(caretOffset + 1)

            AutoPopupController.getInstance(context.project).scheduleAutoPopup(editor)
        }
    }

    private object KphpDocJsonForEncoderInsertHandler : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, element: LookupElement) {
            val editor = context.editor
            val caretOffset = editor.caretModel.offset

            context.document.insertString(caretOffset, " ")
            editor.caretModel.moveToOffset(caretOffset + 1)
        }
    }
}
