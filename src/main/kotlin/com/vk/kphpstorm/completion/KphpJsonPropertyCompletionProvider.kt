package com.vk.kphpstorm.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
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

        val elementBeforeCursor = position.prevSibling
        when (elementBeforeCursor?.text) {
            "=" -> {
                val elementName = elementBeforeCursor.prevSibling?.text ?: return

                val property = KphpJsonTag.properties.firstOrNull { it.name == elementName } ?: return
                property.allowValues?.forEach { value ->
                    result.addElement(LookupElementBuilder.create(value))
                }
            }

            // if no text before cursor
            null -> {
                val isField = owner is Field
                val isClass = owner is PhpClass

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
                    if (property.allowValues != null) {
                        element = element.appendTailText("=", true).withInsertHandler(KphpDocTagJsonInsertHandler)
                    }

                    result.addElement(element)
                }
            }
        }
    }

    private object KphpDocTagJsonInsertHandler : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, element: LookupElement) {
            val caretOffset = context.editor.caretModel.offset

            context.document.insertString(caretOffset, "=")
            context.editor.caretModel.moveToOffset(caretOffset + 1)
        }
    }
}
