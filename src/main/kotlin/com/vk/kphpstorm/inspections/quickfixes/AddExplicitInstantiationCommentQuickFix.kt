package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl
import com.vk.kphpstorm.helpers.setSelectionInEditor

class AddExplicitInstantiationCommentQuickFix(field: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(field) {

    override fun getFamilyName() = "Add explicit generic instantiation tag"
    override fun getText() = "Add explicit generic instantiation tag"

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val placeToInsert = when (startElement) {
            is NewExpression -> startElement.classReference
            is MethodReference -> startElement.nextSibling?.nextSibling?.nextSibling
            is FunctionReference -> startElement.nextSibling
            else -> null
        } ?: return

        val comment = PhpPsiElementFactory.createFromText(project, GenericInstantiationPsiCommentImpl::class.java, "/*<T>*/")
        if (comment != null) {
            val insertedComment = startElement.addAfter(comment, placeToInsert)
            if (editor != null) {
                setSelectionInEditor(editor, insertedComment, 3, 4)
            }
        }
    }
}
