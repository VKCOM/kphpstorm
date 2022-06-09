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

class KphpJsonItemCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
    ) {
        val position = parameters.position
        val owner = position.parentDocComment?.owner as? PhpTypedElement ?: return

        when (position.prevSibling?.text) {
            null -> {
                val isField = owner is Field
                val isClass = owner is PhpClass

                for (jsonElement in KphpJsonTag.jsonElements) {
                    if (jsonElement.allowField == isField || jsonElement.allowClass == isClass) {
                        var element: LookupElementBuilder? = null
                        if (jsonElement.ifType == null) {
                            element = LookupElementBuilder.create(jsonElement.name)
                        } else if (jsonElement.ifType.first.invoke(owner.type.toExPhpType())) {
                            element = LookupElementBuilder.create(jsonElement.name)
                        }

                        if (element == null) {
                            return
                        }

                        if (jsonElement.allowValues != null) {
                            element = element.appendTailText("=", true)
                                .withInsertHandler(KphpDocTagJsonInsertHandler)
                        }

                        result.addElement(element)
                    }
                }
            }
            "=" -> {
                val elementName = position.prevSibling.prevSibling?.text

                val element = KphpJsonTag.jsonElements.singleOrNull { it.name == elementName } ?: return
                for (value in element.allowValues ?: listOf()) {
                    result.addElement(LookupElementBuilder.create(value))
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
