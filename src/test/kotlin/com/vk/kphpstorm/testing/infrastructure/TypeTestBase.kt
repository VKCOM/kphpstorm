package com.vk.kphpstorm.testing.infrastructure

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.helpers.toExPhpType

abstract class TypeTestBase : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/fixtures"

    protected open fun runFixture(vararg fixtureFiles: String) {
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFiles(*fixtureFiles)

        runTypeTest(fixtureFiles)
    }

    protected fun runTypeTest(fixtureFiles: Array<out String>) {
        findExprTypeCalls(fixtureFiles).forEach { call ->
            checkExprTypeCall(call)
        }
    }

    private inline fun <reified T> PsiElement.findChildren(crossinline condition: (PsiElement) -> Boolean): List<T> {
        val result = mutableListOf<T>()
        accept(object : PhpElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (condition(element) && element is T) {
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
        val expectedTypeString = expectedTypePsi.contents
        val gotType = expr.type.global(myFixture.project).toExPhpType()?.let { PsiToExPhpType.dropGenerics(it) }

        val sortedGotType = if (gotType is ExPhpTypePipe)
            ExPhpTypePipe(gotType.items.sortedBy { it.toString() })
        else gotType

        val gotTypeString = sortedGotType.toString().ifEmpty { "<empty>" }

        val file = call.containingFile
        check(gotTypeString == expectedTypeString) {
            """
                In file ${file.name}:${call.line()}
                
                Type mismatch. 
                Expected: $expectedTypeString
                Found: $gotTypeString
                
                
            """.trimIndent()
        }
    }

    private fun findExprTypeCalls(fixtureFiles: Array<out String>): List<FunctionReference> {
        return fixtureFiles.map {
            val file = myFixture.findFileInTempDir(it) ?: return@map emptyList<FunctionReference>()
            myFixture.openFileInEditor(file)

            myFixture.file.findChildren { el ->
                el is FunctionReference && el.name == "expr_type" && el.parameters.size == 2 &&
                        el.parameters.last() is StringLiteralExpression && el.parameters.first() is PhpTypedElement
            }
        }.flatten()
    }

    private fun PhpPsiElement.line(): Int {
        val document = PsiDocumentManager.getInstance(project).getDocument(containingFile)
        val lineNumber = if (document != null) {
            document.getLineNumber(textRange.startOffset) + 1
        } else {
            0
        }
        return lineNumber
    }
}
