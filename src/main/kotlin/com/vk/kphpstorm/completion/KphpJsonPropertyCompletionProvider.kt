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

        when (position.prevSibling?.text) {
            null -> {
                val isField = owner is Field
                val isClass = owner is PhpClass

                for (jsonElement in KphpJsonTag.properties) {
                    if (jsonElement.allowField != isField && jsonElement.allowClass != isClass) {
                        continue
                    }

                    if (jsonElement.ifType != null) {
                        if (!jsonElement.ifType.first.invoke(owner.type.toExPhpType())) {
                            continue
                        }
                    }

                    var element = LookupElementBuilder.create(jsonElement.name)

                    if (jsonElement.allowValues != null) {
                        element = element.appendTailText("=", true).withInsertHandler(KphpDocTagJsonInsertHandler)
                    }

                    result.addElement(element)
                }
            }
            "=" -> {
                val elementName = position.prevSibling.prevSibling?.text

                val element = KphpJsonTag.properties.firstOrNull { it.name == elementName } ?: return
                val allowValues = element.allowValues ?: return
                for (value in allowValues) {
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
