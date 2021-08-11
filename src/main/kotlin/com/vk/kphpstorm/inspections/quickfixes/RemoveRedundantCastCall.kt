package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.FunctionReference

/**
 * Converts 'intval($x)' to '$x'
 */
class RemoveRedundantCastCall : LocalQuickFix {
    override fun getFamilyName() = "[KPHP] Remove redundant cast"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val funcCall = descriptor.psiElement.parent as? FunctionReference ?: return
        val firstArg = funcCall.parameters.takeIf { it.size == 1 }?.first() ?: return

        funcCall.replace(firstArg)
    }
}
