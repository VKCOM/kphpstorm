package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.psi.elements.Function
import com.vk.kphpstorm.helpers.setSelectionInEditor
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class AddReturnTagQuickFix(function: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(function) {

    override fun getFamilyName() = "Add @return tag"
    override fun getText() = "Add @return tag"

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val function = startElement as Function

        val docTag = PhpDocPsiBuilder.addDocTagToReturn(function, project)
        val docType = PsiTreeUtil.getChildOfType(docTag, PhpDocType::class.java)

        // @return {type} — select {type} in the editor
        if (docType != null && editor != null)
            setSelectionInEditor(editor, docType)
    }
}
