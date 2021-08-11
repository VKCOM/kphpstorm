package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.helpers.convertArrayIndexPsiToStringIndexKey

/**
 * Purpose:
 * Having class A { /** @var int */ var $prop; } and $a->prop = [...]
 * we want to report that array is not assignable to int.
 * Type compatibility should take all kphp types into account, like tuples, any, etc.
 * And it should be more precise than built-in (e.g., built-in allows to pass ints as instances, anything as strings).
 *
 * Important! Native inspection "PHP > Type compatibility > Type mismatch in property assignment" must be disabled.
 * @see com.jetbrains.php.lang.inspections.type.PhpFieldAssignmentTypeMismatchInspection
 */
class KphpAssignmentTypeMismatchInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {

            /**
             * lhs = rhs: analyze only if lhs is instance/static field
             */
            override fun visitPhpAssignmentExpression(expr: AssignmentExpression) {
                val lhs = expr.variable

                if (lhs is FieldReference) {
                    val rhs = expr.value as? PhpTypedElement ?: return
                    val project = expr.project
                    val field = lhs.resolve() as? Field

                    if (field != null && !field.isConstant && field !is PhpDocProperty) {
                        // if no @var above lhs field declaration or we could not detect rhs type — well, don't do anything
                        val lhsType = PsiToExPhpType.getFieldDeclaredType(field, project) ?: return
                        val rhsType = PsiToExPhpType.getTypeOfExpr(rhs, project) ?: return

                        if (!lhsType.isAssignableFrom(rhsType, project))
                            holder.registerProblem(expr, "[KPHP] Can't assign '${rhsType.toHumanReadable(expr)}' to '${lhsType.toHumanReadable(expr)}' \$${field.name}")
                    }
                }
                else if (lhs is ArrayAccessExpression && lhs.value is FieldReference) {
                    val rhs = expr.value as? PhpTypedElement ?: return
                    val project = expr.project
                    val field = (lhs.value as FieldReference).resolve() as? Field

                    if (field != null && !field.isConstant && field !is PhpDocProperty) {
                        // if no @var above lhs field declaration or we could not detect rhs type — well, don't do anything
                        val lhsType = PsiToExPhpType.getFieldDeclaredType(field, project) ?: return
                        val rhsType = PsiToExPhpType.getTypeOfExpr(rhs, project) ?: return
                        val indexKey = convertArrayIndexPsiToStringIndexKey(lhs.index) ?: "*"
                        val lhsIndex = lhsType.getSubkeyByIndex(indexKey)

                        if (lhsIndex == null || !lhsIndex.isAssignableFrom(rhsType, project))
                            holder.registerProblem(expr, "[KPHP] Can't assign '${rhsType.toHumanReadable(expr)}' to '${lhsIndex?.toHumanReadable(expr)}' \$${field.name}[*]")
                    }
                }
            }

        }
    }
}
