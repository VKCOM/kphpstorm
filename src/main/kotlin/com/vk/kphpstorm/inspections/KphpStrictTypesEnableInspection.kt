package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.Declare
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.inspections.quickfixes.AddStrictTypesCommentQuickFix

class KphpStrictTypesEnableInspection : PhpInspection() {
    val kphpTagStrictTypeEnable = "@kphp-strict-types-enable"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpElement(element: PhpPsiElement) {
                if (element !is Declare) {
                    return
                }

                if (PhpCodeInsightUtil.getStrictTypeDirectiveValue(element)?.text != "1") {
                    return
                }

                val docComment = element.prevPsiSibling
                if (docComment is PhpDocComment) {
                    if (docComment.getTagElementsByName(kphpTagStrictTypeEnable).isNotEmpty()) {
                        return
                    }
                }

                holder.registerProblem(
                    element,
                    "Missing $kphpTagStrictTypeEnable tag",
                    ProblemHighlightType.WEAK_WARNING,
                    AddStrictTypesCommentQuickFix()
                )
            }
        }
    }
}
