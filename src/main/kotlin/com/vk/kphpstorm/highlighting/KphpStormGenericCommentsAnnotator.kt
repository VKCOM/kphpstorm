package com.vk.kphpstorm.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl

class KphpStormGenericCommentsAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is GenericInstantiationPsiCommentImpl) {
            return
        }

        var unnecessarySpec = false

        val parent = element.parent
        if (parent is FunctionReference) {
            val call = GenericFunctionCall(parent)
            call.resolveFunction()
            if (call.isNoNeedExplicitSpec()) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(TextRange(element.textRange.startOffset, element.textRange.endOffset))
                    .textAttributes(KphpHighlightingData.UNNECESSARY_GENERIC_SPECS).create()
                unnecessarySpec = true
            }
        }

        if (!unnecessarySpec) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(element.textRange.startOffset + 2, element.textRange.endOffset - 2))
                .textAttributes(KphpHighlightingData.GENERIC_SPECS).create()
        }

        val instances = element.extractInstances()
        instances.forEach { (_, instance) ->
            instance.classes(element.project).forEach { _ ->
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(instance.pos)
                    .textAttributes(KphpHighlightingData.PHPDOC_TAG_KPHP)
                    .create()
            }
        }
    }
}
