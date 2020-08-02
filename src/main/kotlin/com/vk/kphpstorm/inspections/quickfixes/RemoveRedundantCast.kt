package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.UnaryExpression

/**
 * Converts '(int)$x' to '$x'
 */
class RemoveRedundantCast : LocalQuickFix {
    override fun getFamilyName() = "Remove redundant cast"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val castExpr = descriptor.psiElement.parent as? UnaryExpression ?: return
        val rhs = castExpr.firstPsiChild ?: return

        castExpr.replace(rhs)
    }
}
