package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericUtil.genericNames

class FunctionGenericNoEnoughInformationInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val call = GenericFunctionCall(reference)
                if (call.function == null) return

                val genericNames = call.function.genericNames()

                if (call.explicitSpecsPsi == null) {
                    genericNames.any {
                        val resolved = call.implicitSpecializationNameMap.contains(it)

                        if (!resolved) {
                            holder.registerProblem(
                                reference.element,
                                "Not enough information to infer generic $it",
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
