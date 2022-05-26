package com.vk.kphpstorm.testing.infrastructure

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.inspections.PhpUndefinedFieldInspection
import com.jetbrains.php.lang.inspections.PhpUndefinedMethodInspection
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import com.vk.kphpstorm.inspections.KphpGenericsInspection
import java.io.File

abstract class AllOkTestBase : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/fixtures"

    override fun setUp() {
        super.setUp()

        myFixture.enableInspections(PhpUndefinedMethodInspection())
        myFixture.enableInspections(PhpUndefinedFieldInspection())
        myFixture.enableInspections(KphpGenericsInspection())
    }

    /**
     * Run inspection on file.fixture.php and check that all <warning> and <error> match
     * If file.qf.php exists, apply quickfixes and compare result to file.qf.php
     */
    protected fun runFixture(vararg fixtureFiles: String) {
        // Highlighting test
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFiles(*fixtureFiles)
        myFixture.testHighlighting(true, false, true)

        // Quick-fix test
        fixtureFiles.forEach { fixtureFile ->
            val qfFile = fixtureFile.replace(".fixture.php", ".qf.php")
            if (File(myFixture.testDataPath + "/" + qfFile).exists()) {
                myFixture.getAllQuickFixes().forEach { myFixture.launchAction(it) }
                myFixture.checkResultByFile(qfFile)
            }
        }
    }
}
