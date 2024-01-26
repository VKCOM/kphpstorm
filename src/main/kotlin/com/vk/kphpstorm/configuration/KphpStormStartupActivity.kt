package com.vk.kphpstorm.configuration

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Implement 'first run' for plugin: after installation (= after restart)
 * detect that plugin was not configured and show a setup dialog window.
 * @see setupKphpStormPluginForProject
 *
 * Note! As a side effect, this is done when opening any project.
 * So, plugin detects if current project is KPHP-based and offers setup only in this case.
 */
class KphpStormStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        DumbService.getInstance(project).runWhenSmart {
            showSetupDialog(project)
        }
    }

    private fun showSetupDialog(project: Project) {
        if (!KphpStormConfiguration.wasSetupForProject(project) && KphpStormConfiguration.seemsLikeProjectIsKphpBased(project)) {
            SetupPluginForProjectDialog(project).show()
        } else {
            if (!KphpStormConfiguration.seemsLikeProjectIsKphpBased(project)) {
                showNotification(project)
            }
        }
    }

    private fun showNotification(project: Project) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("kphpstorm.plugin.setup.notification")
            .createNotification("Transforming to KPHP", NotificationType.INFORMATION)

        val dntShow = "DoNotShowAgain"
        val isKphp = "isKphpProject"

        val propertiesComponent = PropertiesComponent.getInstance(project)
        if (propertiesComponent.getBoolean(dntShow, false)) {
            return
        }

        if (propertiesComponent.getBoolean(isKphp, false)) {
            return
        }

        notification.setSuggestionType(true)

        notification.addAction(object : NotificationAction("Setup project as kPHP") {
            override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                propertiesComponent.setValue(isKphp, true)
                SetupPluginForProjectDialog(project).show()
                notification.expire()
            }
        })

        // Turn-off notifications
        notification.addAction(object : NotificationAction("Don`t show it again") {
            override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                propertiesComponent.setValue(dntShow, true)
                notification.expire()
            }
        })

        Notifications.Bus.notify(notification, project)
    }
}
