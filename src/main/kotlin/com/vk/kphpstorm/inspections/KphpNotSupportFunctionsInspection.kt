package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor

class KphpNotSupportFunctionsInspection : PhpInspection() {
    companion object {
        private val PHP_TO_KPHP_FUNCTIONS = mapOf(
            "end" to "array_last_value",
            "reset" to "array_first_value",
        )
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val functionName = reference.name
                if (functionName !in PHP_TO_KPHP_FUNCTIONS.keys)
                    return

                holder.registerProblem(
                    reference,
                    "KPHP does not support $functionName(), maybe use ${PHP_TO_KPHP_FUNCTIONS[functionName]}()?",
                )
            }
        }
    }
}