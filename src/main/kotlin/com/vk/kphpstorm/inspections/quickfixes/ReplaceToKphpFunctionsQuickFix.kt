package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.FunctionReference

class ReplaceToKphpFunctionsQuickFix(
    private val replaceFunction: String,
    private val isFirst: Boolean = false
) : LocalQuickFix {
    override fun getFamilyName(): String {
        return if (isFirst) {
            "Maybe use $replaceFunction()"
        } else {
            "Replace with $replaceFunction()"
        }
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val unsupportedFunction = descriptor.psiElement
        if (unsupportedFunction !is FunctionReference)
            return

        val newFunctionNameNode = unsupportedFunction.nameNode ?: return

        val newFunction = PhpPsiElementFactory.createFromText(
            project,
            PhpTokenTypes.IDENTIFIER,
            replaceFunction
        ).node

        unsupportedFunction.node.replaceChild(
            newFunctionNameNode,
            newFunction
        )
    }
}
