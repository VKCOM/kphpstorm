package com.vk.kphpstorm.inspections.prettifier

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocVariable
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag
import com.jetbrains.php.lang.psi.PhpPsiElementFactory

class SwapTypeAndVarNamePrettification(
        private val docTag: PhpDocParamTag
) : PhpDocPrettification {
    private val tagNameWithAt get() = docTag.name
    private val varName get() = docTag.varName

    override fun getActionText() = "Swap type and \$$varName"
    override fun getDescriptionText() = "Use $tagNameWithAt {type} $$varName, not $tagNameWithAt $$varName {type}"
    override fun getHighlightElement() = docTag.firstChild!!
    override fun getHightlightType() = ProblemHighlightType.WARNING

    override fun applyPrettification() {
        val curDocType = docTag.firstPsiChild?.nextPsiSibling as? PhpDocType ?: return
        val curVarName = PsiTreeUtil.getChildOfType(docTag, PhpDocVariable::class.java) ?: return

        val newDocTag = PhpPsiElementFactory.createPhpDocTag(docTag.project, "@param ${curDocType.text} $$varName")
        val newDocType = newDocTag.firstPsiChild ?: return
        val newVarName = newDocType.nextPsiSibling ?: return

        // I couldn't get it work to replace() existing elements by each other
        // found workaround — to create new ones
        curDocType.replace(newVarName)
        curVarName.replace(newDocType)
    }
}
