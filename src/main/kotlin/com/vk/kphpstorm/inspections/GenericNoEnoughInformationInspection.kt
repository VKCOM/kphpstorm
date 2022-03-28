package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
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

class GenericNoEnoughInformationInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpNewExpression(expression: NewExpression) {
                val call = GenericConstructorCall(expression)
                checkGenericCall(call, expression)
            }

            override fun visitPhpMethodReference(reference: MethodReference) {
                val call = GenericMethodCall(reference)
                checkGenericCall(call, reference.firstChild.nextSibling.nextSibling)
            }

            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val call = GenericFunctionCall(reference)
                checkGenericCall(call, reference.firstChild)
            }

            private fun checkGenericCall(call: GenericCall, errorPsi: PsiElement) {
                if (!call.isResolved()) return
                val genericNames = call.genericNames()

                if (call.explicitSpecsPsi == null) {
                    genericNames.any { decl ->
                        val resolved = call.implicitSpecializationNameMap.contains(decl.name)

                        if (!resolved) {
                            holder.registerProblem(
                                errorPsi,
                                "Not enough information to infer generic ${decl.name}",
                                ProblemHighlightType.GENERIC_ERROR
                            )

                            return@any true
                        }

                        false
                    }
                }
            }
        }
    }
}
