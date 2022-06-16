package com.vk.kphpstorm.testing.infrastructure

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import com.vk.kphpstorm.configuration.KphpStormConfiguration


abstract class IntentionTestBase(
        private val intentionToExecute: IntentionAction
) : BasePlatformTestCase() {

    open val languageLevel: PhpLanguageLevel = PhpLanguageLevel.PHP740

    override fun getTestDataPath() = "src/test/fixtures"

    private fun setupLanguageLevel() {
        val projectConfigurationFacade = PhpProjectConfigurationFacade.getInstance(project)
        projectConfigurationFacade.languageLevel = languageLevel
    }

    /**
     * Run intention on file.fixture.php at place marked <caret>
     * file.qf.php must exist, the result of applying intention is compared to its contents
     */
    protected fun runIntention(fixtureFile: String) {
        setupLanguageLevel()

        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFile(fixtureFile)
        myFixture.launchAction(myFixture.findSingleIntention(intentionToExecute.familyName))

        val qfFile = fixtureFile.replace(".fixture.php", ".qf.php")
        myFixture.checkResultByFile(qfFile)
    }
}
