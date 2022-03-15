package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericUtil.genericNames

class FunctionGenericInstantiationArgsCountMismatchInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val call = GenericFunctionCall(reference)
                if (call.function == null) return

                val countGenericNames = call.function.genericNames().size
                val countExplicitSpecs = call.explicitSpecs.size

                if (countGenericNames != countExplicitSpecs && call.explicitSpecsPsi != null) {
                    holder.registerProblem(
                        call.explicitSpecsPsi,
                        "$countGenericNames type arguments expected for ${call.function.fqn}",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                }
            }
        }
    }
}
