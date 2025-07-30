package com.vk.kphpstorm.testing.infrastructure

import com.intellij.codeInsight.intention.IntentionAction
import com.vk.kphpstorm.configuration.KphpStormConfiguration

abstract class IntentionTestBase(private val intentionToExecute: IntentionAction) : KphpStormTestBase() {

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

    /**
     * Assert there are no intention [intentionToExecute] in file [fixtureFile]
     */
    protected fun assertNoIntention(fixtureFile: String) {
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFile(fixtureFile)
        val availableIntentions = myFixture
            .availableIntentions
            .filter { intentionAction -> intentionAction.familyName == intentionToExecute.familyName }

        assertEmpty(availableIntentions)
    }
}
