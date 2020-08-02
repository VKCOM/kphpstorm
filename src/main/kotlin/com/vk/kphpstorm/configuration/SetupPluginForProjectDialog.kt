package com.vk.kphpstorm.configuration

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.playback.commands.ActionCommand
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

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
        setResizable(false)
        init()
    }

    override fun createActions() =
            if (isKphpProject && !isAlreadySetup) arrayOf(okAction)
            else arrayOf(okAction, cancelAction)

    override fun createCenterPanel(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            val textMarkup = when {
                isAlreadySetup -> """
                    <html>
                    <p>It seems that KPHPStorm plugin was <b>already configured</b><br>for project ${project.name}</p><br>
                    <p>But if you made some changes to KPHPStorm inspections<br>or something doesn't work as expected,</p>
                    </html>
                """.trimIndent()
                !isKphpProject -> """
                    <html>
                    <p>It seems that your project uses <b>regular PHP</b>, not KPHP.</p>
                    <p>But even though you can enjoy strict typing inspections.</p><br>
                    <p>Just press this button to disable some native inspections<br>and enable new ones.</p>
                    </html>
                """.trimIndent()
                else             -> """
                    <html>
                    <p>With <b>KPHPStorm plugin</b> coding will be much easier!</p>
                    <ul>
                    <li>tuples and shapes support</li>
                    <li>@kphp- tags autocomplete</li>
                    <li>strict typing and better type inferring</li>
                    <li>... and much more!</li>
                    </ul>
                    </html>
                """.trimIndent()
            }

            preferredSize = Dimension(400, 120)
            add(JLabel(textMarkup, SwingConstants.LEFT))
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
