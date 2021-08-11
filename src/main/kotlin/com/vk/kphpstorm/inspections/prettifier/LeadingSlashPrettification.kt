package com.vk.kphpstorm.inspections.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType

class LeadingSlashPrettification(
        private val docType: PhpDocType
) : PhpDocPrettification {
    override fun getActionText() = "Remove leading \\"
    override fun getDescriptionText() = "[KPHP] Leading slash is not necessary"
    override fun getHighlightElement() = docType
    override fun getHightlightType() = ProblemHighlightType.WEAK_WARNING

    override fun applyPrettification() {
        if (docType.prevSibling.elementType == PhpDocTokenTypes.DOC_NAMESPACE)
            docType.prevSibling.delete()
    }
}
