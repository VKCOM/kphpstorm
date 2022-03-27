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
import com.jetbrains.rd.util.first
import com.vk.kphpstorm.generics.GenericCall
import com.vk.kphpstorm.generics.GenericConstructorCall
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericMethodCall

class GenericSeveralGenericTypesInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {
            override fun visitPhpNewExpression(expression: NewExpression) {
                val call = GenericConstructorCall(expression)
                checkGenericCall(call, expression)
            }

            override fun visitPhpMethodReference(reference: MethodReference) {
                val call = GenericMethodCall(reference)
                checkGenericCall(call, reference)
            }

            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val call = GenericFunctionCall(reference)
                checkGenericCall(call, reference)
            }

            private fun checkGenericCall(call: GenericCall, element: PsiElement) {
                if (!call.isResolved()) return

                // В случае даже если есть ошибки, то мы показываем их только
                // в случае когда нет явного определения шаблона для вызова функции.
                if (call.implicitSpecializationErrors.isNotEmpty() && !call.withExplicitSpecs()) {
                    val error = call.implicitSpecializationErrors.first()
                    val (type1, type2) = error.value

                    val genericsTString = call.genericTs.joinToString(", ")
                    val callString = element.text

                    val firstBracketIndex = callString.indexOf('(')
                    val beforeBracket = callString.substring(0, firstBracketIndex)
                    val afterBracket = callString.substring(firstBracketIndex + 1)
                    val callStingWithGenerics = "$beforeBracket/*<$genericsTString>*/($afterBracket"

                    val explanation =
                        "Please, provide all generics types using following syntax: $callStingWithGenerics;"

                    holder.registerProblem(
                        element,
                        "Couldn't reify generic <${error.key}> for call: it's both $type1 and $type2\n$explanation",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                }
            }
        }
    }
}
