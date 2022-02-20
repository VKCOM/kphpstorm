package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.inspections.quickfixes.ReplaceToKphpFunctionsQuickFix

class KphpNotSupportFunctionsInspection : PhpInspection() {
    companion object {
        private val PHP_TO_KPHP_FUNCTIONS = mapOf(
            "end" to listOf("array_last_value", "array_last_key"),
            "reset" to listOf("array_first_value", "array_first_key"),
        )
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val functionName = reference.name
                if (functionName !in PHP_TO_KPHP_FUNCTIONS.keys)
                    return

                val fixes = mutableListOf<LocalQuickFix>()

                val maybeFunction = PHP_TO_KPHP_FUNCTIONS[functionName]
                maybeFunction?.forEach { maybeFunctionName ->
                    fixes.add(ReplaceToKphpFunctionsQuickFix(maybeFunctionName))
                }

                holder.registerProblem(
                    reference,
                    "KPHP does not support $functionName()",
                    *fixes.toTypedArray(),
                )
            }
        }
    }
}
