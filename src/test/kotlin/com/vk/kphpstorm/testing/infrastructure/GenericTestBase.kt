package com.vk.kphpstorm.testing.infrastructure

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.inspections.PhpUndefinedFieldInspection
import com.jetbrains.php.lang.inspections.PhpUndefinedMethodInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.inspections.KphpGenericsInspection
import com.vk.kphpstorm.inspections.KphpParameterTypeMismatchInspection
import com.vk.kphpstorm.inspections.KphpUndefinedClassInspection
import java.io.File

abstract class GenericTestBase : BasePlatformTestCase() {

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

        findExprTypeCalls(fixtureFiles).forEach { call ->
            checkExprTypeCall(call)
        }
    }

    private inline fun <reified T> PsiElement.findChildren(crossinline condition: (PsiElement) -> Boolean): List<T> {
        val result = mutableListOf<T>()
        accept(object : PhpElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (condition(element) && (element is T)) {
                    result.add(element)
                }

                var child = element.firstChild
                while (child != null) {
                    child.accept(this)
                    child = child.nextSibling
                }
            }
        })
        return result
    }

    private fun checkExprTypeCall(call: FunctionReference) {
        val expr = call.parameters.first() as PhpTypedElement
        val expectedTypePsi = call.parameters.last() as StringLiteralExpression
        val expectedType = expectedTypePsi.contents
        val type = expr.type.global(myFixture.project).toExPhpType()?.let { PsiToExPhpType.dropGenerics(it) }

        val sortedType = if (type is ExPhpTypePipe)
            ExPhpTypePipe(type.items.sortedBy { it.toString() })
        else type

        val typeString = sortedType.toString().ifEmpty { "<empty>" }

        // TODO: add location (line and test name)
        check(typeString == expectedType) {
            "Type mismatch. Expected: $expectedType, found: $typeString"
        }
    }

    private fun findExprTypeCalls(fixtureFiles: Array< out String>): List<FunctionReference> {
        return fixtureFiles.map {
            val file = myFixture.findFileInTempDir(it) ?: return@map emptyList<FunctionReference>()
            myFixture.openFileInEditor(file)

            myFixture.file.findChildren { el ->
                el is FunctionReference && el.name == "expr_type" && el.parameters.size == 2 &&
                        el.parameters.last() is StringLiteralExpression && el.parameters.first() is PhpTypedElement
            }
        }.flatten()
    }
}
