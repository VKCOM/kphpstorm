package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Parameter
import com.vk.kphpstorm.helpers.setSelectionInEditor
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class AddParamTagQuickFix(parameter: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(parameter) {

    override fun getFamilyName() = "Add @param tag"
    override fun getText() = "Add @param tag"

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val parameter = startElement as Parameter
        val function = PhpPsiUtil.getParentByCondition(parameter, true, Function.INSTANCEOF, null) as? Function ?: return

        val docTag = PhpDocPsiBuilder.addDocTagToParameter(parameter, function, project)
        val docType = PsiTreeUtil.getChildOfType(docTag, PhpDocType::class.java)

        // @param {type} $var_name — select {type} in the editor
        if (docType != null && editor != null)
            setSelectionInEditor(editor, docType)
    }
}
