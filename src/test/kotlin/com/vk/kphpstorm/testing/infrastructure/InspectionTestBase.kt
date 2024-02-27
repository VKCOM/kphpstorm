package com.vk.kphpstorm.testing.infrastructure

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import com.jetbrains.php.lang.inspections.PhpInspection
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import com.vk.kphpstorm.configuration.setupKphpStormPluginForProject
import java.io.File


abstract class InspectionTestBase(
    private val inspectionToEnable: PhpInspection? = null,
) : BasePlatformTestCase() {

    open val languageLevel: PhpLanguageLevel = PhpLanguageLevel.PHP740

    override fun getTestDataPath() = "src/test/fixtures"

    override fun setUp() {
        super.setUp()

        if (inspectionToEnable != null) {
            myFixture.enableInspections(inspectionToEnable)
        }
    }

    private fun setupLanguageLevel() {
        val projectConfigurationFacade = PhpProjectConfigurationFacade.getInstance(project)
        projectConfigurationFacade.languageLevel = languageLevel
    }

    /**
     * Run inspection on file.fixture.php and check that all <warning> and <error> match
     * If file.qf.php exists, apply quickfixes and compare result to file.qf.php
     */
    protected fun runFixture(fixtureFile: String) {
        setupLanguageLevel()
        setupKphpStormPluginForProject(project)

        // Highlighting test
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFile(fixtureFile)
        myFixture.testHighlighting(true, false, true)

        // Quick-fix test
        val qfFile = fixtureFile.replace(".fixture.php", ".qf.php")
        if (File(myFixture.testDataPath + "/" + qfFile).exists()) {
            myFixture.getAllQuickFixes().forEach { action ->
                val available = action.isAvailable(project, myFixture.editor, myFixture.file)
                if (available) {
                    myFixture.launchAction(action.asIntention())
                }
            }

            myFixture.checkResultByFile(qfFile)
        }
    }
}
