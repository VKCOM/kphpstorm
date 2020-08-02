package com.vk.kphpstorm.testing.infrastructure

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.inspections.PhpInspection
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import java.io.File


abstract class InspectionTestBase(
        private val inspectionToEnable: PhpInspection
) : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/fixtures"

    override fun setUp() {
        super.setUp()

        myFixture.enableInspections(inspectionToEnable)
    }

    /**
     * Run inspection on file.fixture.php and check that all <warning> and <error> match
     * If file.qf.php exists, apply quickfixes and compare result to file.qf.php
     */
    protected fun runFixture(fixtureFile: String) {
        // Highlighting test
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFile(fixtureFile)
        myFixture.testHighlighting(true, false, true)

        // Quick-fix test
        val qfFile = fixtureFile.replace(".fixture.php", ".qf.php")
        if (File(myFixture.testDataPath + "/" + qfFile).exists()) {
            myFixture.getAllQuickFixes().forEach { myFixture.launchAction(it) }
            myFixture.checkResultByFile(qfFile)
        }
    }
}
