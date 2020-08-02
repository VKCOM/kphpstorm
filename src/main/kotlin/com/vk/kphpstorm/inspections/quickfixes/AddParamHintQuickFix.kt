package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Parameter
import com.vk.kphpstorm.helpers.setSelectionInEditor

class AddParamHintQuickFix(parameter: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(parameter) {

    override fun getFamilyName() = "Add type hint"
    override fun getText() = "Add type hint"

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val parameter = startElement as Parameter
        if (parameter.typeDeclaration != null) return

        val anchor = parameter.firstChild        // no type hint => this is variable or variadic
        val fakeParameter = PhpPsiElementFactory.createComplexParameter(project, "type \$tmp")
        val typeDeclaration = parameter.addBefore(fakeParameter.firstChild, anchor)

        // {type} $var_name — select {type} in the editor
        if (editor != null)
            setSelectionInEditor(editor, typeDeclaration)
    }
}
