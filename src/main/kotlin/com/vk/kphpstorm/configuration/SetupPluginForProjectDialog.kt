package com.vk.kphpstorm.configuration

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.playback.commands.ActionCommand
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import java.awt.Dimension
import javax.swing.JComponent

/**
 * Dialog with "OK" button that performs auto-setup KPHPStorm plugin for current project.
 * After setup, it invokes "Invalidate Caches and Restart" prompt, assuming that user clicks main button there.
 */
class SetupPluginForProjectDialog(private val project: Project) : DialogWrapper(false) {
    private val isAlreadySetup = KphpStormConfiguration.wasSetupForProject(project)
    private val isKphpProject = KphpStormConfiguration.seemsLikeProjectIsKphpBased(project)

    init {
        title = "Setup KPHPStorm for Project"
        okAction.putValue("Name", "Setup KPHPStorm, then 'Invalidate and Restart'")
        isResizable = false
        init()
    }

    override fun createActions() =
            if (isKphpProject && !isAlreadySetup) arrayOf(okAction)
            else arrayOf(okAction, cancelAction)

    override fun createCenterPanel(): JComponent {
        return panel {
            when {
                isAlreadySetup -> {
                    row {
                        label("It seems that KPHPStorm plugin was already configured for project ${project.name}")
                    }
                    row {
                        label("But if you made some changes to KPHPStorm inspections or something doesn't work as expected,")
                    }
                }
                !isKphpProject -> {
                    row {
                        label("It seems that your project uses regular PHP, not KPHP.")
                    }
                    row {
                        label("But even though you can enjoy strict typing inspections.")
                    }
                    row {
                        label("Just press this button to disable some native inspections and enable new ones.")
                    }
                }
                else -> {
                    row {
                        label("With KPHPStorm plugin coding will be much easier!")
                    }
                    bulletPointRow("tuples and shapes support")
                    bulletPointRow("@kphp- tags autocomplete")
                    bulletPointRow("strict typing and better type inferring")
                    bulletPointRow("... and much more!")
                }
            }
        }.apply {
            preferredSize = Dimension(400, 120)
        }
    }

    private fun Panel.bulletPointRow(text: String) {
        row {
            label("• $text")
        }
    }

    override fun doOKAction() {
        setupKphpStormPluginForProject(project)
        showInvalidateCachesWindow()
    }

    private fun showInvalidateCachesWindow() {
        val actionId = "InvalidateCaches"
        val action = ActionManager.getInstance().getAction(actionId)
        ActionManager.getInstance().tryToExecute(action, ActionCommand.getInputEvent(actionId), null, ActionPlaces.UNKNOWN, true)
    }
}
