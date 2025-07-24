package com.vk.kphpstorm.intentions.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class RemoveDocTagPrettification(
        private val docTag: PhpDocTag,
        private val descriptionText: String,
        private val actionText: String,
        private val highlightType: ProblemHighlightType = ProblemHighlightType.WEAK_WARNING
) : PhpDocPrettification {
    override fun getActionText() = actionText
    override fun getDescriptionText() = descriptionText
    override fun getHighlightElement() = docTag.firstChild!!
    override fun getHightlightType() = highlightType

    override fun applyPrettification() {
        PhpDocPsiBuilder.removeTagFromDocComment(docTag)
    }
}
