package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.generics.GenericCall
import com.vk.kphpstorm.generics.GenericConstructorCall
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericMethodCall
import com.vk.kphpstorm.inspections.quickfixes.RemoveExplicitGenericSpecsQuickFix

class GenericUnnecessaryExplicitInstantiationListInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpNewExpression(expression: NewExpression) {
                val call = GenericConstructorCall(expression)
                checkGenericCall(call)
            }

            override fun visitPhpMethodReference(reference: MethodReference) {
                val call = GenericMethodCall(reference)
                checkGenericCall(call)
            }

            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val call = GenericFunctionCall(reference)
                checkGenericCall(call)
            }

            private fun checkGenericCall(call: GenericCall) {
                if (!call.isResolved()) return
                if (call.explicitSpecsPsi == null) return

                if (call.isNoNeedExplicitSpec()) {
                    holder.registerProblem(
                        call.explicitSpecsPsi!!,
                        "Remove unnecessary explicit list of instantiation arguments",
                        ProblemHighlightType.WEAK_WARNING,
                        RemoveExplicitGenericSpecsQuickFix()
                    )
                }
            }
        }
    }
}
