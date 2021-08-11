package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Function

object KphpColorDocTag : KphpDocTag("@kphp-color") {
    override val description: String
        get() = "[KPHP] Used for coloring functions to fire compilation error if color mixing mismatches palette rules."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Function
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return owner is Function
    }

    override fun areDuplicatesAllowed(): Boolean {
        // @kphp-color can meet several times, for each color
        return true
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null) {
            holder.errTag(docTag, "[KPHP] Specify a color from the palette (with an optional comment after)")
            return
        }
        // highlight a color name with bold italic (no customization, no validation that color exists)
        val text = rhs.text
        val spacePos = text.indexOf(' ')
        val colorLen = if (spacePos == -1) text.length else spacePos
        val offset = rhs.textOffset
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .enforcedTextAttributes(TextAttributes().apply { fontType = 3 }).range(TextRange(offset, offset + colorLen)).create()
    }

    override fun onAutoCompleted(docComment: PhpDocComment): String? {
        return "|red"
    }
}
