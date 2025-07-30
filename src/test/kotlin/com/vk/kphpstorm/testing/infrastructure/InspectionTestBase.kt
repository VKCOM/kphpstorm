package com.vk.kphpstorm.testing.infrastructure

import com.jetbrains.php.lang.inspections.PhpInspection
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import com.vk.kphpstorm.configuration.setupKphpStormPluginForProject
import java.io.File

abstract class InspectionTestBase(private val inspectionToEnable: PhpInspection? = null) : KphpStormTestBase() {

    override fun setUp() {
        super.setUp()

        if (inspectionToEnable != null) {
            myFixture.enableInspections(inspectionToEnable)
        }
    }

    /**
     * Run inspection on file.fixture.php and check that all <warning> and <error> match
     * If file.qf.php exists, apply quickfixes and compare result to file.qf.php
     */
    protected fun runFixture(fixtureFile: String) {
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
