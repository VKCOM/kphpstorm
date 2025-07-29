package com.vk.kphpstorm.testing.infrastructure

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.config.PhpProjectConfigurationFacade


abstract class KphpStormTestBase() : BasePlatformTestCase() {

    protected open val languageLevel: PhpLanguageLevel = PhpLanguageLevel.PHP740

    override fun getTestDataPath() = "src/test/fixtures"

    override fun setUp() {
        super.setUp()
        setupLanguageLevel()
    }

    private fun setupLanguageLevel() {
        val projectConfigurationFacade = PhpProjectConfigurationFacade.getInstance(project)
        projectConfigurationFacade.languageLevel = languageLevel
    }
}
