package com.vk.kphpstorm.inspections

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.playback.commands.ActionCommand
import com.intellij.psi.PsiElement
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl

class CollapseGenericsInstantiationIntention : PsiElementBaseIntentionAction() {
    override fun getText() = "Collapse generics instantiation"
    override fun getFamilyName() = "Collapse generics instantiation"
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement) =
        element is GenericInstantiationPsiCommentImpl

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val actionId = "CollapseRegion"
        val action = ActionManager.getInstance().getAction(actionId)
        ActionManager.getInstance()
            .tryToExecute(action, ActionCommand.getInputEvent(actionId), null, ActionPlaces.UNKNOWN, true)
    }
}
