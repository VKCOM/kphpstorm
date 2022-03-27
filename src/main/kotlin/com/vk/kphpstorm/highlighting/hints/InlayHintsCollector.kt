package com.vk.kphpstorm.highlighting.hints

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.vk.kphpstorm.generics.GenericFunctionCall

@Suppress("UnstableApiUsage")
class InlayHintsCollector(
    editor: Editor,
    private val file: PsiFile,
    private val settings: GenericsInlayTypeHintsProvider.Settings,
    private val sink: InlayHintsSink
) : FactoryInlayHintsCollector(editor) {

    private val myHintsFactory = InlayHintPresentationFactory(editor)

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        // If the indexing process is in progress.
        if (file.project.service<DumbService>().isDumb) return true

        when {
            element is FunctionReference && settings.showForFunctions -> {
                showAnnotation(element)
            }
        }

        return true
    }

    private fun showAnnotation(element: FunctionReference) {
        val call = GenericFunctionCall(element)
        if (!call.isGeneric() || call.withExplicitSpecs()) {
            return
        }

        val genericNames = call.genericNames().joinToString(", ")
        val simplePresentation = myHintsFactory.inlayHint("<$genericNames>")

        val namePsi = element.firstChild
        val offset = if (element is MethodReference) {
            // offset after "->method_name"
            namePsi.nextSibling.nextSibling.endOffset
        } else {
            namePsi.endOffset
        }

        sink.addInlineElement(offset, false, simplePresentation, false)
    }
}
