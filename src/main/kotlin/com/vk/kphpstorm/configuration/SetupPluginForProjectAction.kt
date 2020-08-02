package com.vk.kphpstorm.configuration

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SetupPluginForProjectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return
        SetupPluginForProjectDialog(project).show()

    }
}
