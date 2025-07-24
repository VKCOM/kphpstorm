package com.vk.kphpstorm.intentions.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.Function
import com.vk.kphpstorm.helpers.getOwnerSmart
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class MoveReturnTagToTypeHintPrettification(
        private val docTag: PhpDocReturnTag,
        private val typeHintS: String
) : PhpDocPrettification {
    override fun getActionText() = "Remove @return, set '$typeHintS' type hint"
    override fun getDescriptionText() = "@return can be replaced with type hint '$typeHintS'"
    override fun getHighlightElement() = docTag.firstChild!!
    override fun getHightlightType() = ProblemHighlightType.WEAK_WARNING

    override fun applyPrettification() {
        val function = docTag.parentDocComment!!.getOwnerSmart() as? Function ?: return

        val rParen = PhpPsiUtil.getChildOfType(function, PhpTokenTypes.chRPAREN) ?: return
        function.addAfter(PhpPsiElementFactory.createReturnType(docTag.project, typeHintS), rParen)
        function.addAfter(PhpPsiElementFactory.createColon(docTag.project), rParen)

        PhpDocPsiBuilder.removeTagFromDocComment(docTag)
    }
}
