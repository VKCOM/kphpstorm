package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpModifier
import com.vk.kphpstorm.helpers.setSelectionInEditor

class AddVarHintQuickFix(field: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(field) {

    override fun getFamilyName() = "[KPHP] Add type declaration"
    override fun getText() = "Add type declaration"

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val field = startElement as Field

        val tmpField = PhpPsiElementFactory.createClassField(project, PhpModifier.PUBLIC_FINAL_DYNAMIC, "f", null, "type")
        field.parent.addBefore(tmpField.firstPsiChild!!.nextPsiSibling!!, field)
        field.parent.addBefore(PhpPsiElementFactory.createWhiteSpace(project), field)
        val typeDeclaration = field.typeDeclaration

        // {type} $field_name — select {type} in the editor
        if (editor != null && typeDeclaration != null)
            setSelectionInEditor(editor, typeDeclaration)
    }
}
