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
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.vk.kphpstorm.generics.GenericCall
import com.vk.kphpstorm.generics.GenericConstructorCall
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericMethodCall
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.kphptags.psi.toHumanReadable

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
            element is MethodReference && settings.showForFunctions -> {
                val call = GenericMethodCall(element)
                showAnnotation(call, element.firstChild?.nextSibling?.nextSibling)
            }
            element is FunctionReference && settings.showForFunctions -> {
                val call = GenericFunctionCall(element)
                showAnnotation(call, element.firstChild)
            }
            element is NewExpression && settings.showForFunctions -> {
                val call = GenericConstructorCall(element)
                showAnnotation(call, element.firstChild?.nextSibling?.nextSibling)
            }
        }

        return true
    }

    private fun showAnnotation(call: GenericCall, place: PsiElement?) {
        if (place == null || !call.isGeneric() || call.withExplicitSpecs()) {
            return
        }

        // Показываем хинт только если удалось вывести типы.
        val decl = call.isNotEnoughInformation()
        if (decl != null) {
            return
        }

        val genericNames = if (call is GenericConstructorCall) {
            call.genericNames()
        } else {
            call.function()!!.genericNames()
        }.joinToString(", ") {
            it.toHumanReadable()
        }

        if (genericNames.isEmpty()) {
            return
        }

        val simplePresentation = myHintsFactory.inlayHint("<$genericNames>")

        sink.addInlineElement(place.endOffset, false, simplePresentation, false)
    }
}
