package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpNamespaceReference

class ReplaceUnsupportedFunctionCallQuickFix(
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

        val unsupportedFunctionNameNode = unsupportedFunction.nameNode ?: return

        val newFunctionNameNode = PhpPsiElementFactory.createFromText(
            project,
            PhpTokenTypes.IDENTIFIER,
            replaceFunction
        ).node

        if (unsupportedFunction.namespaceName == "\\") {
            val namespaceReference = PsiTreeUtil.findChildOfType(unsupportedFunction, PhpNamespaceReference::class.java)
            namespaceReference?.delete()
        }

        unsupportedFunction.node.replaceChild(
            unsupportedFunctionNameNode,
            newFunctionNameNode
        )
    }
}
