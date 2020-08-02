package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.Field
import com.vk.kphpstorm.inspections.helpers.PhpDocPsiBuilder

class AddKphpSerializedFieldQuickFix : LocalQuickFix {

    override fun getFamilyName() = "Add @kphp-serialized-field"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val field = descriptor.psiElement.parent as Field

        PhpDocPsiBuilder.addKphpSerializedToField(field, project)
    }

}
