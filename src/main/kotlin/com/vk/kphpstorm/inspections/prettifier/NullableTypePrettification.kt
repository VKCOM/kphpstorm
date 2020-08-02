package com.vk.kphpstorm.inspections.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.psi.PhpPsiElementFactory

class NullableTypePrettification(
        private val docType: PhpDocType,
        private val replacementStr: String
) : PhpDocPrettification {
    override fun getActionText() = "Replace with $replacementStr"
    override fun getDescriptionText() = "Use '?T', not 'T|null'"
    override fun getHighlightElement() = docType
    override fun getHightlightType() = ProblemHighlightType.WEAK_WARNING

    override fun applyPrettification() {
        docType.replace(PhpPsiElementFactory.createPhpDocType(docType.project, replacementStr))
    }
}
