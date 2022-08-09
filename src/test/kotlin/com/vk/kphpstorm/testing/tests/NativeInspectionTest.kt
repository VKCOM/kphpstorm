package com.vk.kphpstorm.testing.tests

import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.jetbrains.php.lang.inspections.codeSmell.PhpMethodOrClassCallIsNotCaseSensitiveInspection
import com.vk.kphpstorm.configuration.nativeInspectionCustomLevel
import com.vk.kphpstorm.testing.infrastructure.InspectionTestBase

class NativeInspectionTest : InspectionTestBase() {

    private fun enableInspection(inspection: InspectionProfileEntry) {
        myFixture.enableInspections(inspection)

        val key = HighlightDisplayKey.find(inspection.shortName)
        val level = nativeInspectionCustomLevel[key.id] ?: throw AssertionError("key '${key.id}' not found")

        val profile = ProjectInspectionProfileManager.getInstance(project).currentProfile
        profile.setErrorLevel(key, level, project)
    }


    fun `test PhpMethodOrClassCallIsNotCaseSensitive inspection`() {
        enableInspection(PhpMethodOrClassCallIsNotCaseSensitiveInspection())

        runFixture("native_inspection/case_sensitive.good.fixture.php")
    }

}
