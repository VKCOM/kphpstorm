package com.vk.kphpstorm.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder
import com.vk.kphpstorm.kphptags.KphpImmutableClassDocTag

class AddImmutableClassAnnotationIntention : PsiElementBaseIntentionAction() {
    override fun getText(): @IntentionName String = "Add @kphp-immutable-class"
    override fun getFamilyName(): @IntentionFamilyName String = getText()

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement
    ): Boolean {
        if (!element.isClassNameNode()) {
            return false
        }

        val klass = element.parent as PhpClass
        val klassDocNode = klass.docComment ?: return true

        // do not suggest if already present
        return !KphpImmutableClassDocTag.existsInDocComment(klassDocNode)
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement
    ) {
        val klass = element.parent as PhpClass
        PhpDocPsiBuilder.addTagToClass(klass, KphpImmutableClassDocTag)
    }

    private fun PsiElement.isClassNameNode(): Boolean {
        val klass = this.parent as? PhpClass ?: return false
        return klass.nameIdentifier == this
    }
}