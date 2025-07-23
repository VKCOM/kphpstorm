package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.vk.kphpstorm.intentions.prettifier.PhpDocPrettification

class PrettifySomethingInDocBlockQuickFix(private val prettification: PhpDocPrettification) : LocalQuickFix {

    override fun getFamilyName() = prettification.getActionText()

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        prettification.applyPrettification()
    }
}
