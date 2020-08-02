package com.vk.kphpstorm.testing.infrastructure

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.vk.kphpstorm.configuration.KphpStormConfiguration


abstract class IntentionTestBase(
        private val intentionToExecute: IntentionAction
) : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/fixtures"

    /**
     * Run intention on file.fixture.php at place marked <caret>
     * file.qf.php must exist, the result of applying intention is compared to its contents
     */
    protected fun runIntention(fixtureFile: String) {
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFile(fixtureFile)
        myFixture.launchAction(myFixture.findSingleIntention(intentionToExecute.familyName))

        val qfFile = fixtureFile.replace(".fixture.php", ".qf.php")
        myFixture.checkResultByFile(qfFile)
    }
}
