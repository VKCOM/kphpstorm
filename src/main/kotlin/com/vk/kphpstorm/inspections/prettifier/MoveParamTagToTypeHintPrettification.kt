package com.vk.kphpstorm.inspections.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.Function
import com.vk.kphpstorm.helpers.getOwnerSmart
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class MoveParamTagToTypeHintPrettification(
        private val docTag: PhpDocParamTag,
        private val typeHintS: String
) : PhpDocPrettification {
    override fun getActionText() = "Remove @param, set '$typeHintS' type hint"
    override fun getDescriptionText() = "@param can be replaced with type hint '$typeHintS'"
    override fun getHighlightElement() = docTag.firstChild!!
    override fun getHightlightType() = ProblemHighlightType.WEAK_WARNING

    override fun applyPrettification() {
        val function = docTag.parentDocComment!!.getOwnerSmart() as? Function ?: return

        val varName = docTag.varName
        val fArg = function.parameters.find { it.name == varName } ?: return
        val anchor = fArg.firstChild        // no type hint => this is variable or variadic

        val fakeParameter = PhpPsiElementFactory.createComplexParameter(docTag.project, "$typeHintS \$tmp")
        var child = fakeParameter.firstChild
        while (child.elementType != PhpTokenTypes.VARIABLE) {
            fArg.addBefore(child, anchor)
            child = child.nextSibling
        }

        PhpDocPsiBuilder.removeTagFromDocComment(docTag)
    }
}

