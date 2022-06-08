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

    protected fun runFixture(vararg fixtureFiles: String) {
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFiles(*fixtureFiles)

        val exprTypeCalls = findExprTypeCalls(fixtureFiles)
        exprTypeCalls.forEach { call ->
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

        val file = call.containingFile
        check(typeString == expectedType) {
            """
                In file ${file.name}:${call.line()}
                
                Type mismatch. 
                Expected: $expectedType
                Found: $typeString
                
                
            """.trimIndent()
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
