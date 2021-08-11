package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.Field
import com.vk.kphpstorm.helpers.setSelectionInEditor

class AddVarDefaultValueQuickFix(field: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(field) {

    override fun getFamilyName() = "[KPHP] Add default value"
    override fun getText() = "Add default value"

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val field = startElement as Field

        val statement = PhpPsiElementFactory.createStatement(project, "\$a = 0")
        var child = statement.firstChild.firstChild.nextSibling
        var anchor = field.nameIdentifier
        while (child != null) {
            anchor = field.addAfter(child, anchor)
            child = child.nextSibling
        }
        val defaultValue = field.defaultValue

        // $field_name = 0 — select "0" in the editor
        if (editor != null && defaultValue != null)
            setSelectionInEditor(editor, defaultValue)
    }
}
