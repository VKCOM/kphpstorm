package com.vk.kphpstorm.configuration

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex

object KphpStormConfiguration {
    private const val WAS_SETUP_FOR_PROJECT_V_1_0 = "kphpstorm.was_setup_v_1_0"

    fun wasSetupForProject(project: Project): Boolean {
        return PropertiesComponent.getInstance(project).getBoolean(WAS_SETUP_FOR_PROJECT_V_1_0)
    }

    fun saveThatSetupForProjectDone(project: Project) {
        PropertiesComponent.getInstance(project).setValue(WAS_SETUP_FOR_PROJECT_V_1_0, true)
    }

    fun seemsLikeProjectIsKphpBased(project: Project): Boolean {
        return PhpIndex.getInstance(project).getFunctionsByName("tuple").isNotEmpty()
                && PhpIndex.getInstance(project).getFunctionsByName("shape").isNotEmpty()
    }
}
