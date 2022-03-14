package com.vk.kphpstorm.highlighting.hints

import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.impl.EditorImpl

@Suppress("UnstableApiUsage")
class InlayHintPresentationFactory(private val myEditor: Editor) {
    private val myTextMetricsStorage = InlayTextMetricsStorage(myEditor as EditorImpl)
    private val myOffsetFromTopProvider = object : InsetValueProvider {
        override val top = myTextMetricsStorage.getFontMetrics(true).offsetFromTop()
    }

    fun inlayHint(value: String): InlayPresentation {
        return roundWithBackground(text(value))
    }

    private fun text(text: String): InlayPresentation {
        return withInlayAttributes(TextInlayPresentation(myTextMetricsStorage, false, text),
            DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT_HIGHLIGHTED)
    }

    private fun roundWithBackground(base: InlayPresentation): InlayPresentation {
        val rounding = RoundWithBackgroundPresentation(
            InsetPresentation(
                base, left = 7, right = 7,
                top = 0, down = 0
            ),
            0, 0, backgroundAlpha = 0f
        )

        return DynamicInsetPresentation(rounding, myOffsetFromTopProvider)
    }

    private fun withInlayAttributes(
        base: InlayPresentation,
        attributes: TextAttributesKey = DefaultLanguageHighlighterColors.INLAY_DEFAULT
    ): InlayPresentation {
        return WithAttributesPresentation(
            base, attributes, myEditor,
            WithAttributesPresentation.AttributesFlags().withIsDefault(true)
        )
    }
}
