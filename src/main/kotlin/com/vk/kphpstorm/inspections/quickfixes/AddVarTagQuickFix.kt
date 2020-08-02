package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.psi.elements.Field
import com.vk.kphpstorm.helpers.setSelectionInEditor
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class AddVarTagQuickFix(field: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(field) {

    override fun getFamilyName() = "Add @var tag"
    override fun getText() = "Add @var tag"

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val field = startElement as Field

        val docTag = PhpDocPsiBuilder.addDocTagToField(field, project)
        val docType = PsiTreeUtil.getChildOfType(docTag, PhpDocType::class.java)

        // @var {type} $var_name — select {type} in the editor
        if (docType != null && editor != null)
            setSelectionInEditor(editor, docType)
    }
}
