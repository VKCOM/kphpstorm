package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.Function
import com.vk.kphpstorm.helpers.setSelectionInEditor

class AddReturnHintQuickFix(function: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(function) {

    override fun getFamilyName() = "Add return hint"
    override fun getText() = "Add return hint"

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val function = startElement as Function
        if (function.typeDeclaration != null) return

        val rParen = PhpPsiUtil.getChildOfType(function, PhpTokenTypes.chRPAREN) ?: return
        function.addAfter(PhpPsiElementFactory.createReturnType(project, "type"), rParen)
        function.addAfter(PhpPsiElementFactory.createColon(project), rParen)
        val typeDeclaration = function.typeDeclaration

        // : {type} — select {type} in the editor
        if (editor != null && typeDeclaration != null)
            setSelectionInEditor(editor, typeDeclaration)
    }
}
