package com.vk.kphpstorm.testing.infrastructure

import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import com.jetbrains.php.lang.psi.stubs.PhpFileStubBuilder
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import java.io.File
import java.nio.file.Paths


abstract class StubTestBase : BasePlatformTestCase() {

    open val languageLevel: PhpLanguageLevel = PhpLanguageLevel.PHP740

    override fun getTestDataPath() = "src/test/fixtures"

    private val stubBuilder = PhpFileStubBuilder()


    override fun setUp() {
        super.setUp()
        VfsRootAccess.allowRootAccess(testRootDisposable, Paths.get(testDataPath).toAbsolutePath().toString())
    }

    private fun setupLanguageLevel() {
        val projectConfigurationFacade = PhpProjectConfigurationFacade.getInstance(project)
        projectConfigurationFacade.languageLevel = languageLevel
    }

    /**
     * Run stub test on the file.fixture.php
     * file.stub.php must exist. It contains dumped stub tree
     */
    protected fun doStubTest(fixtureFile: String) {
        setupLanguageLevel()

        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFile(fixtureFile)

        val stubTree = stubBuilder.buildStubTree(myFixture.file)
        val stubTreeString = DebugUtil.stubTreeToString(stubTree)

        val expectedFileRelPath = fixtureFile.replace(".fixture.php", ".stub.php")
        if (fixtureFile == expectedFileRelPath) {
            fail("wrong input fixture file path: $fixtureFile")
        }

        val expectedFile = File(testDataPath).resolve(expectedFileRelPath)
        if (!expectedFile.exists() || !expectedFile.isFile) {
            expectedFile.createNewFile()
            expectedFile.writeText(stubTreeString)
            fail("new .stub.php file was created")
        }

        assertSameLinesWithFile(expectedFile.absolutePath, stubTreeString)
    }
}
