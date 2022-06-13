package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project

class RemoveExplicitGenericSpecsQuickFix : LocalQuickFix {
    override fun getFamilyName() = "Remove explicit specs"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val comment = descriptor.psiElement ?: return
        comment.delete()
    }
}
