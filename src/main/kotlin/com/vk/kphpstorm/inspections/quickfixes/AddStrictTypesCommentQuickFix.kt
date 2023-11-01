package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory

class AddStrictTypesCommentQuickFix : LocalQuickFix {
    override fun getFamilyName() = "Add @kphp-strict-types-enable tag"
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val declareElement = descriptor.startElement
        val docComment = PhpPsiElementFactory.createFromText(
            project, PhpDocElementTypes.DOC_COMMENT,
            "/** @kphp-strict-types-enable */"
        )

        declareElement.parent.addBefore(docComment, declareElement)
    }
}
