package com.vk.kphpstorm.inspections.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class RemoveDocTagPrettification(
        private val docTag: PhpDocTag,
        private val descriptionText: String,
        private val actionText: String,
        private val highlightType: ProblemHighlightType = ProblemHighlightType.WEAK_WARNING
) : PhpDocPrettification {
    override fun getActionText() = "[KPHP] " + actionText
    override fun getDescriptionText() = "[KPHP] " + descriptionText
    override fun getHighlightElement() = docTag.firstChild!!
    override fun getHightlightType() = highlightType

    override fun applyPrettification() {
        PhpDocPsiBuilder.removeTagFromDocComment(docTag)
    }
}
