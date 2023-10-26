package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.Declare

class AddStrictTypesCommentQuickFix(declare: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(declare) {

    override fun getFamilyName() = "Add @kphp-strict-types-enabled tag"
    override fun getText() = "Add @kphp-strict-types-enabled tag"

    override fun invoke(
        project: Project,
        file: PsiFile,
        editor: Editor?,
        startElement: PsiElement,
        endElement: PsiElement
    ) {
        val declareElement = startElement as Declare
        val docComment = PhpPsiElementFactory.createFromText(
            project, PhpDocElementTypes.DOC_COMMENT,
            "/** @kphp-strict-types-enabled */"
        )

        declareElement.parent.addBefore(docComment, declareElement)
    }
}