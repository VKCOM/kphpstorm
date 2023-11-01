package com.vk.kphpstorm.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.psi.PhpPsiElementFactory

class AddStrictTypesCommentQuickFix : LocalQuickFix {

    override fun getFamilyName() = "Add @kphp-strict-types-enable tag"
    override fun applyFix(p0: Project, p1: ProblemDescriptor) {
        val declareElement = p1.startElement
        val docComment = PhpPsiElementFactory.createFromText(
            p0, PhpDocElementTypes.DOC_COMMENT,
            "/** @kphp-strict-types-enable */"
        )

        declareElement.parent.addBefore(docComment, declareElement)
    }
}
