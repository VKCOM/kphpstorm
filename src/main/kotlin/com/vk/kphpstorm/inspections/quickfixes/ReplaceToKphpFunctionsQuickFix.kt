package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.FunctionReference

class ReplaceToKphpFunctionsQuickFix(private val maybeFunctionName: String) : LocalQuickFix {
    override fun getFamilyName(): String {
        return "Replace with $maybeFunctionName()"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val unsupportedFunction = descriptor.psiElement
        if (unsupportedFunction !is FunctionReference) {
            return
        }

        val methodNameNode = unsupportedFunction.nameNode ?: return

        val newFunction = PhpPsiElementFactory.createFromText(
            project,
            PhpTokenTypes.IDENTIFIER,
            maybeFunctionName
        ).node

        unsupportedFunction.node.replaceChild(
            methodNameNode,
            newFunction
        )
    }
}
