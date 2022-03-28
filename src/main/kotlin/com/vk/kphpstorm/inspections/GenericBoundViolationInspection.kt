package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.generics.GenericCall
import com.vk.kphpstorm.generics.GenericConstructorCall
import com.vk.kphpstorm.generics.GenericFunctionCall
import com.vk.kphpstorm.generics.GenericMethodCall
import com.vk.kphpstorm.helpers.toExPhpType

class GenericBoundViolationInspection : PhpInspection() {
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
                val genericNames = call.genericNames()

                genericNames.forEach { decl ->
                    val (resolvedType, isExplicit) = if (call.specializationNameMap[decl.name] != null) {
                        call.specializationNameMap[decl.name] to true
                    } else {
                        call.implicitSpecializationNameMap[decl.name] to false
                    }

                    if (resolvedType == null) return@forEach

                    val upperBoundClass =
                        PhpIndex.getInstance(call.project).getAnyByFQN(decl.extendsClass).firstOrNull()
                            ?: return@forEach
                    val upperBoundClassType = PhpType().add(upperBoundClass.fqn).toExPhpType() ?: return@forEach

                    val errorPsi = call.explicitSpecsPsi ?: call.callArgs.firstOrNull() ?: return@forEach

                    if (!upperBoundClassType.isAssignableFrom(resolvedType, call.project)) {
                        val extendsOrImplements = if (upperBoundClass.isInterface) "implements" else "extends"

                        val message =
                            "${if (isExplicit) "Explicit" else "Reified"} generic type for ${decl.name} is not within its bounds (${resolvedType} not $extendsOrImplements ${upperBoundClass.fqn})"

                        holder.registerProblem(
                            errorPsi,
                            message,
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                }
            }
        }
    }
}
