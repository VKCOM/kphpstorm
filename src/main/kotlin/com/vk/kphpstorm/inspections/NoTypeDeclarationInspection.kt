package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.inspections.helpers.KphpTypingAnalyzer
import com.vk.kphpstorm.inspections.quickfixes.*

/**
 * Checks that types are present everywhere they should be
 * 1) all class fields have type or default value
 * 2) all functions and methods are strictly typed (@param/@return or type hints)
 */
class NoTypeDeclarationInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {

            override fun visitPhpField(field: Field) {
                val nameIdentifier = field.nameIdentifier ?: return

                if (field.isConstant)
                    return

                if (!KphpTypingAnalyzer.doesFieldHaveType(field))
                    holder.registerProblem(nameIdentifier, "[KPHP] All fields must have @var phpdoc or default value", AddVarTagQuickFix(field), AddVarHintQuickFix(field), AddVarDefaultValueQuickFix(field))
            }

            override fun visitPhpMethod(method: Method) {
                if (KphpTypingAnalyzer.isFunctionStrictlyTyped(method))
                    checkStrictlyTypedFunction(method)
            }

            override fun visitPhpFunction(function: Function) {
                if (KphpTypingAnalyzer.isFunctionStrictlyTyped(function))
                    checkStrictlyTypedFunction(function)
            }

            private fun checkStrictlyTypedFunction(function: Function) {
                val nameIdentifier = function.nameIdentifier ?: function

                // check that all parameters are typed
                for (parameter in function.parameters) {
                    val pName = parameter.nameIdentifier ?: continue
                    if (PsiToExPhpType.getArgumentDeclaredType(parameter, holder.project) == null)
                        holder.registerProblem(pName, "[KPHP] Declare type hint or @param for this argument", AddParamTagQuickFix(parameter), AddParamHintQuickFix(parameter))
                }

                // check that function return value is typed
                if (PsiToExPhpType.getReturnDeclaredType(function, holder.project) == null && !KphpTypingAnalyzer.isFunctionInferredReturnTypeVoid(function))
                    holder.registerProblem(nameIdentifier, "[KPHP] Declare return hint or @return in phpdoc", AddReturnTagQuickFix(function), AddReturnHintQuickFix(function))
            }
        }
    }
}
