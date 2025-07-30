package com.vk.kphpstorm.testing.infrastructure

import com.intellij.psi.impl.DebugUtil
import com.jetbrains.php.lang.psi.stubs.PhpFileStubBuilder
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import java.io.File


abstract class StubTestBase : KphpStormTestBase() {

    private val stubBuilder = PhpFileStubBuilder()

    /**
     * Run stub test on the file.fixture.php
     * file.stub.php must exist. It contains dumped stub tree
     */
    protected fun doStubTest(fixtureFile: String) {
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
