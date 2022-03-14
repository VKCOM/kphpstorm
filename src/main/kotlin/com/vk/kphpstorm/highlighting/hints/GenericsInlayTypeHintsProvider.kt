package com.vk.kphpstorm.highlighting.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.ImmediateConfigurable.Case
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import javax.swing.JComponent
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class GenericsInlayTypeHintsProvider : InlayHintsProvider<GenericsInlayTypeHintsProvider.Settings> {
    data class Settings(
        var showForFunctions: Boolean = true,
    )

    override val key: SettingsKey<Settings> = KEY
    override val name: String = "KPHP generic annotations"
    override val previewText: String = ""

    override fun createConfigurable(settings: Settings) = object : ImmediateConfigurable {
        override val mainCheckboxText: String = "Use inline hints for visibility"

        override val cases: List<Case> = listOf(
            Case("Show for functions", "functions", settings::showForFunctions),
        )

        override fun createComponent(listener: ChangeListener): JComponent = JPanel()
    }

    override fun createSettings() = Settings()

    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink) =
        InlayHintsCollector(editor, file, settings, sink)

    companion object {
        private val KEY: SettingsKey<Settings> = SettingsKey("kphp.generic.annotations")
    }
}
