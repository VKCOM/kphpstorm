package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.parser.PhpElementTypes
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeArray
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.inspections.quickfixes.RemoveRedundantCast
import com.vk.kphpstorm.inspections.quickfixes.RemoveRedundantCastCall

/**
 * Reports cases like:
 * * (int)$expr_already_int
 * * intval($expr_already_int)
 * Same for other casts.
 */
class RedundantCastInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {

            override fun visitPhpUnaryExpression(expr: UnaryExpression) {
                if (expr.elementType != PhpElementTypes.CAST_EXPRESSION)
                    return

                if (isRedundantCast(expr.firstChild.elementType!!, expr.firstPsiChild ?: return))
                    holder.registerProblem(expr.firstChild!!, "Redundant cast", RemoveRedundantCast())
            }

            override fun visitPhpFunctionCall(reference: FunctionReference) {
                val isCastFunction = reference !is MethodReference && reference.name.let {
                    it == "intval" || it == "floatval" || it == "strval" || it == "boolval"
                }
                if (!isCastFunction)
                    return

                val rhs = reference.parameters.takeIf { it.size == 1 }?.first() ?: return
                val correspondingToken = when (reference.name) {
                    "intval"   -> PhpTokenTypes.opINTEGER_CAST
                    "floatval" -> PhpTokenTypes.opFLOAT_CAST
                    "strval"   -> PhpTokenTypes.opSTRING_CAST
                    "boolval"  -> PhpTokenTypes.opBOOLEAN_CAST
                    else       -> return
                }

                if (isRedundantCast(correspondingToken, rhs))
                    holder.registerProblem(reference.nameNode!!.psi, "Redundant cast call", RemoveRedundantCastCall())
            }
        }
    }

    private fun isRedundantCast(castToken: IElementType, rhs: PsiElement): Boolean {
        // (int)$int_arr[$i] is ok, not redundant, because of strict kphp typing
        // (0 in kphp, null in php on unexisting key, which later produces compilation error about this)
        if (rhs is ArrayAccessExpression || rhs !is PhpTypedElement)
            return false

        val rhsType = rhs.type.toExPhpType(rhs.project) ?: return false

        return when (castToken) {
            PhpTokenTypes.opINTEGER_CAST -> rhsType === ExPhpType.INT
            PhpTokenTypes.opFLOAT_CAST   -> rhsType === ExPhpType.FLOAT
            PhpTokenTypes.opSTRING_CAST  -> rhsType === ExPhpType.STRING
            PhpTokenTypes.opBOOLEAN_CAST -> rhsType === ExPhpType.BOOL || rhsType === ExPhpType.FALSE
            PhpTokenTypes.opARRAY_CAST   -> rhsType is ExPhpTypeArray || rhsType is ExPhpTypePipe && rhsType.items.all { it is ExPhpTypeArray }
            else                         -> false
        }
    }
}
