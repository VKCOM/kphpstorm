package com.vk.kphpstorm.inspections.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.PhpPsiElementFactory

class ConvertVarToSingleLinePrettification(
        private val docComment: PhpDocComment
) : PhpDocPrettification {
    override fun getActionText() = "Convert @var to single line"
    override fun getDescriptionText() = "Convert @var to single line"
    override fun getHighlightElement() = docComment
    override fun getHightlightType() = ProblemHighlightType.WEAK_WARNING

    override fun applyPrettification() {
        val varTag = docComment.firstPsiChild ?: return

        val newText = "/** ${varTag.text} */"
        val newDocComment = PhpPsiElementFactory.createPhpPsiFromText(docComment.project, PhpDocComment::class.java, newText)
        docComment.replace(newDocComment)
    }
}
