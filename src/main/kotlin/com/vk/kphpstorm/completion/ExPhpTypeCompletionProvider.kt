package com.vk.kphpstorm.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType

/**
 * Suggest 'tuple' and 'shape' inside a type in @param/@var/@return.
 * Also filter out some standard completions that mess with our intentions.
 */
class ExPhpTypeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (PsiTreeUtil.findFirstParent(parameters.position, PhpDocType.INSTANCEOF) == null)
            return

        result.addElement(LookupElementBuilder.create("tuple").bold())
        result.addElement(LookupElementBuilder.create("shape").bold())
        result.addElement(LookupElementBuilder.create("future"))
        result.addElement(LookupElementBuilder.create("future_queue"))
        result.addElement(LookupElementBuilder.create("any"))

        // "Cassandra\Tuple" messes with our 'tuple', I don't want it to be seen, filter it out
        // same for some others also
        result.runRemainingContributors(parameters) { t ->
            val messesWithKphpTypes = t.lookupElement.lookupString.let {
                it == "Tuple" || it == "TableUpdate" || it == "SWFShape" || it == "integer" || it == "boolean" || it == "Float_" || it == "Int64"
            }
            if (!messesWithKphpTypes)
                result.addElement(t.lookupElement)
        }
    }
}
