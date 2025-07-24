package com.vk.kphpstorm.intentions.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpFieldType
import com.jetbrains.php.lang.psi.elements.PhpModifier
import com.vk.kphpstorm.helpers.getOwnerSmart
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class MoveVarTagToFieldHintPrettification(
        private val docTag: PhpDocParamTag,
        private val typeHintS: String
) : PhpDocPrettification {
    override fun getActionText() = "Remove @var, set '$typeHintS' field hint"
    override fun getDescriptionText() = "@var can be replaced with field hint '$typeHintS'"
    override fun getHighlightElement() = docTag.firstChild!!
    override fun getHightlightType() = ProblemHighlightType.WEAK_WARNING

    override fun applyPrettification() {
        val field = docTag.parentDocComment!!.getOwnerSmart() as? Field ?: return

        val tmpField = PhpPsiElementFactory.createClassField(docTag.project, PhpModifier.PUBLIC_FINAL_DYNAMIC, "f", null, typeHintS)
        val typeEl = tmpField.children.filterIsInstance<PhpFieldType>()
        if (typeEl.isNotEmpty()) {
            field.parent.addBefore(tmpField.firstPsiChild!!.nextPsiSibling!!, field)
            field.parent.addBefore(PhpPsiElementFactory.createWhiteSpace(docTag.project), field)
        }

        PhpDocPsiBuilder.removeTagFromDocComment(docTag)
    }
}
