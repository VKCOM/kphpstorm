package com.vk.kphpstorm.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.kphptags.KphpWarnPerformanceDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocWarnPerformanceItemPsiImpl

/**
 * Completing arguments for '@kphp-warn-performance' and '@kphp-analyze-performance': all, !implicit-array-cast, etc.
 */
class KphpWarnPerformanceItemCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val docTag = PsiTreeUtil.getParentOfType(parameters.position, PhpDocTag::class.java) ?: return
        val items = mutableListOf<String>()
        var allMentioned = false

        var item = docTag.firstChild
        while (item != null) {
            if (item is KphpDocWarnPerformanceItemPsiImpl) {
                val name = item.name

                if (name == "all")
                    allMentioned = true
                else if (name.isNotEmpty())
                    items.add(name)
            }
            item = item.nextSibling
        }

        // if 'all' exists — complete with negation
        if (allMentioned) {
            KphpWarnPerformanceDocTag.AVAILABLE_ITEMS.forEach {
                if (!items.contains(it))
                    result.addElement(LookupElementBuilder.create("!$it"))
            }
        }
        // if 'all' doesn't exist — complete 'all' and available items
        else {
            if (items.isEmpty())
                result.addElement(LookupElementBuilder.create("all"))
            KphpWarnPerformanceDocTag.AVAILABLE_ITEMS.forEach {
                if (!items.contains(it))
                    result.addElement(LookupElementBuilder.create(it))
            }
        }
    }
}
