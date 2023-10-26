package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.Declare
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.impl.DeclareImpl
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.inspections.quickfixes.AddStrictTypesCommentQuickFix

class KphpStrictTypesEnableInspection : PhpInspection() {
    val strictTypeEnable: CharSequence = "strict_types=1"
    val kphpTagStrictTypeEnable: CharSequence = "@kphp-strict-types-enabled"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpElement(element: PhpPsiElement?) {
                if (element is Declare && element.text.contains(strictTypeEnable)) {
                    val psiElements = element.context?.children
                    if (psiElements != null) {
                        for (i in psiElements.indices) {
                            if (i >= 1 && psiElements[i] is DeclareImpl && psiElements[i] == element) {
                                for (j in i downTo 0) {
                                    if (psiElements[j] is PhpDocCommentImpl && psiElements[j].text.contains(kphpTagStrictTypeEnable))
                                        return
                                }
                            }
                        }
                    }

                    holder.registerProblem(
                        element,
                        "Missing @kphp-strict-types-enabled tag",
                        ProblemHighlightType.WEAK_WARNING,
                        AddStrictTypesCommentQuickFix(element)
                    )
                }
            }
        }
    }
}