package com.vk.kphpstorm.testing.infrastructure

import com.jetbrains.php.lang.inspections.PhpUndefinedFieldInspection
import com.jetbrains.php.lang.inspections.PhpUndefinedMethodInspection
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import com.vk.kphpstorm.inspections.KphpGenericsInspection
import com.vk.kphpstorm.inspections.KphpParameterTypeMismatchInspection
import com.vk.kphpstorm.inspections.KphpUndefinedClassInspection
import java.io.File

abstract class GenericTestBase : TypeTestBase() {

    override fun getTestDataPath() = "src/test/fixtures"

    override fun setUp() {
        super.setUp()

        myFixture.enableInspections(PhpUndefinedMethodInspection())
        myFixture.enableInspections(PhpUndefinedFieldInspection())
        myFixture.enableInspections(KphpUndefinedClassInspection())
        myFixture.enableInspections(KphpGenericsInspection())
        myFixture.enableInspections(KphpParameterTypeMismatchInspection())
    }

    /**
     * Run inspection on file.fixture.php and check that all <warning> and <error> match
     * If file.qf.php exists, apply quickfixes and compare result to file.qf.php
     */
    override fun runFixture(vararg fixtureFiles: String) {
        // Highlighting test
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.testHighlighting(true, false, true, *fixtureFiles)

        // Quick-fix test
        fixtureFiles.forEach { fixtureFile ->
            val qfFile = fixtureFile.replace(".fixture.php", ".qf.php")
            if (qfFile != fixtureFile && File(myFixture.testDataPath + "/" + qfFile).exists()) {
                myFixture.getAllQuickFixes().forEach { myFixture.launchAction(it) }
                myFixture.checkResultByFile(qfFile)
            }
        }

        runTypeTest(fixtureFiles)
    }
}
