package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.vk.kphpstorm.kphptags.psi.KphpDocWarnPerformanceItemPsiImpl

class RemoveWarnPerformanceItemQuickFix(
        item: KphpDocWarnPerformanceItemPsiImpl,
        private val text: String
) : LocalQuickFixAndIntentionActionOnPsiElement(item) {

    override fun getFamilyName() = "[KPHP] " + text
    override fun getText() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        startElement.delete()
    }
}
