package com.vk.kphpstorm.configuration

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
        if (!KphpStormConfiguration.wasSetupForProject(project) && KphpStormConfiguration.seemsLikeProjectIsKphpBased(project))
            SetupPluginForProjectDialog(project).show()
    }
}
