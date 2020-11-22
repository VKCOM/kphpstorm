package com.vk.kphpstorm.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.util.ProcessingContext

/**
 * Suggest '#ifndef KPHP' and '#endif'
 * Couldn't get it work on typing, only after Ctrl + Space hotkey.
 */
class IfndefEndifCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position as? LeafElement ?: return
        if (!position.text.startsWith('#'))     // not '//' comment
            return

        result.addElement(LookupElementBuilder.create("ifndef KPHP").bold())
        result.addElement(LookupElementBuilder.create("endif").bold())
    }
}
