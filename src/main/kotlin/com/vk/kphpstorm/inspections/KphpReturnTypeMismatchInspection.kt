package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.PhpReturn
import com.jetbrains.php.lang.psi.elements.impl.FunctionImpl
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.helpers.getOwnerSmart
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.inspections.helpers.KphpTypingAnalyzer

/**
 * Reports an error on 'return ...' statements
 * if return type doesn't match with declaration: in phpdoc @return or type hint.
 * Also analyzes @return phpdoc and highlights if it differs from actual.
 *
 * Important! Native inspection "PHP > Type compatibility > Incompatible return type" must be disabled.
 * @see com.jetbrains.php.lang.inspections.type.PhpIncompatibleReturnTypeInspection
 */
class KphpReturnTypeMismatchInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {

            override fun visitPhpFunction(function: Function) {
                if (function.isClosure && FunctionImpl.isShortArrowFunction(function)) {
                    val returnValue = FunctionImpl.getShortArrowFunctionArgument(function) ?: return
                    checkReturnValue(function, returnValue, returnValue)
                }
            }

            override fun visitPhpReturn(returnStatement: PhpReturn) {
                val function = PsiTreeUtil.getParentOfType(returnStatement, Function::class.java) ?: return
                checkReturnValue(function, returnStatement.argument, returnStatement)
            }

            private fun checkReturnValue(function: Function, returnValue: PsiElement?, elementToHighlight: PsiElement) {
                val project = holder.project
                val expectedType = PsiToExPhpType.getReturnDeclaredType(function, project)
                        ?: (if (KphpTypingAnalyzer.isFunctionStrictlyTyped(function)) ExPhpType.VOID else return)

                val actualType =
                        if (returnValue == null) ExPhpType.VOID
                        else PsiToExPhpType.getTypeOfExpr(returnValue, project) ?: return

                if (!expectedType.isAssignableFrom(actualType, project)) {
                    holder.registerProblem(elementToHighlight, "[KPHP] Can't return '${actualType.toHumanReadable(function)}', expected '${expectedType.toHumanReadable(function)}'", ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
                }
            }

            override fun visitPhpDocTag(tag: PhpDocTag) {
                if (tag is PhpDocReturnTag) {
                    val function = tag.parentDocComment?.getOwnerSmart() as? Function ?: return
                    visitPhpDocReturnTag(function, tag)
                }
            }

            private fun visitPhpDocReturnTag(function: Function, tag: PhpDocReturnTag) {
                val project = holder.project
                val hintType = function.typeDeclaration?.type?.toExPhpType(project)
                val docType = tag.type.toExPhpType(project)

                val compareWithInferred = docType != null && (hintType == null || hintType === ExPhpType.ARRAY_OF_ANY)
                if (compareWithInferred) {
                    val inferredType = function.inferredType.toExPhpType(project)
                    if (inferredType != null && !docType!!.isAssignableFrom(inferredType, project)) {
                        holder.registerProblem(tag.firstChild, "[KPHP] Actual return type is '${inferredType.toHumanReadable(tag)}'", ProblemHighlightType.GENERIC_ERROR)
                    }
                }
            }
        }
    }
}
