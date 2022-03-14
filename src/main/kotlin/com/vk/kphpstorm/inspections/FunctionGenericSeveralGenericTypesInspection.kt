package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.jetbrains.rd.util.first
import com.vk.kphpstorm.generics.GenericFunctionCall

class FunctionGenericSeveralGenericTypesInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val call = GenericFunctionCall(reference)
                call.resolveFunction()
                if (call.function == null) return

                // В случае даже если есть ошибки, то мы показываем их только
                // в случае когда нет явного определения шаблона для вызова функции.
                if (call.implicitSpecializationErrors.isNotEmpty() && !call.withExplicitSpecs()) {
                    val error = call.implicitSpecializationErrors.first()
                    val (type1, type2) = error.value

                    val genericsTString = call.genericTs.joinToString(", ")
                    val callString = reference.element.text
                    val parts = callString.split("(")
                    val callStingWithGenerics = parts[0] + "<$genericsTString>(" + parts[1]

                    val explanation =
                        "Please, provide all generics types using following syntax: $callStingWithGenerics;"

                    holder.registerProblem(
                        reference.parameterList ?: reference.element,
                        "Couldn't reify generic <${error.key}> for call: it's both $type1 and $type2\n$explanation",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                }
            }
        }
    }
}
