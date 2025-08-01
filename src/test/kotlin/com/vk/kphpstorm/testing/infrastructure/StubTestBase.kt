package com.vk.kphpstorm.testing.infrastructure

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.stubs.Stub
import com.intellij.psi.stubs.StubElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
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
        val stubTreeString = dumpToString(stubTree)

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

    private fun dumpToString(node: StubElement<*>): String {
        val buffer = StringBuilder()
        dumpToString(node, buffer, 0)

        return buffer.toString()
    }

    private fun dumpToString(node: StubElement<*>, buffer: StringBuilder, indent: Int) {
        StringUtil.repeatSymbol(buffer, ' ', indent)

        val presentable = getPresentable(node)

        if (presentable != null) {
            buffer.append(presentable.toString()).append(':')
        }

        buffer.append(node.toString()).append('\n')

        for (child in node.childrenStubs) {
            dumpToString(child, buffer, indent + 2)
        }
    }

    @Suppress("UnstableApiUsage")
    private fun getPresentable(node: Stub): Any? {
        return when (node) {
            is PsiFileStubImpl<*> -> {
                null
            }

            is PhpDocTagStub -> {
                "${node.elementType} {name: ${node.name}${if (node.value != null) ", value: " + node.value else ""}}"
            }

            else -> {
                if (node is StubElement<*>) node.elementType else node.stubSerializer
            }
        }
    }
}
