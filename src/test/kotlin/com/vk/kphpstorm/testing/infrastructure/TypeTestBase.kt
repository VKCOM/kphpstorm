package com.vk.kphpstorm.testing.infrastructure

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.configuration.KphpStormConfiguration
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.helpers.toExPhpType

abstract class TypeTestBase : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/fixtures"

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

    protected fun runFixture(vararg fixtureFiles: String) {
        KphpStormConfiguration.saveThatSetupForProjectDone(project)
        myFixture.configureByFiles(*fixtureFiles)

        val exprTypeCalls = findExprTypeCalls(fixtureFiles)

        exprTypeCalls.forEach { call ->
            checkExprTypeCall(call)
        }
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
