package com.vk.kphpstorm.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.inspections.PhpInspection
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression
import com.jetbrains.php.lang.psi.elements.ForeachStatement
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeAny
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.ExPhpTypePrimitive
import com.vk.kphpstorm.helpers.toExPhpType

/**
 * Reports invalid [ $indexing ] in various scenarious:
 * * invalid argument supplied for foreach  (including 'kmixed' support)
 * * invalid index of array/string indexing (including 'kmixed' support)
 * * invalid index access of non-indexable types (including 'tuple' and 'shape' support)
 *
 * Important! Some native inspections must be disabled:
 * [com.jetbrains.php.lang.inspections.type.PhpWrongForeachArgumentTypeInspection]
 * [com.jetbrains.php.lang.inspections.type.PhpIllegalArrayKeyTypeInspection]
 * [com.jetbrains.php.lang.inspections.type.PhpIllegalStringOffsetInspection]
 */
class ArrayAndIndexingInspection : PhpInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PhpElementVisitor() {

            override fun visitPhpForeach(foreach: ForeachStatement) {
                val argument = foreach.argument
                if (argument is PhpTypedElement) {
                    val argumentType = argument.type.toExPhpType(holder.project) ?: return

                    if (!isOkArgumentForForeach(argumentType, holder.project))
                        holder.registerProblem(argument, "[KPHP] Invalid foreach on '${argumentType.toHumanReadable(foreach)}'")
                }
            }

            override fun visitPhpArrayAccessExpression(expression: ArrayAccessExpression) {
                val arrayIndex = expression.index?.firstPsiChild
                if (arrayIndex != null && arrayIndex is PhpTypedElement) {
                    val indexType = arrayIndex.type.toExPhpType(holder.project) ?: ExPhpType.ANY

                    if (!isOkArrayIndex(indexType, holder.project))
                        holder.registerProblem(expression.index!!, "[KPHP] Invalid index of type '${indexType.toHumanReadable(expression)}'")
                }

                val value = expression.value
                if (value != null && value is PhpTypedElement) {
                    val valueType = value.type.toExPhpType(holder.project) ?: ExPhpType.ANY

                    if (!isOkVariableForIndexing(valueType, holder.project))
                        holder.registerProblem(expression, "[KPHP] Invalid indexing of '${valueType.toHumanReadable(expression)}'")
                }
            }

        }
    }

    // Traversable/Iterator/generators not supported
    fun isOkArgumentForForeach(type: ExPhpType, project: Project): Boolean =
            type.isAssignableFrom(ExPhpType.ARRAY_OF_ANY, project)

    // ArrayAccess not supported
    @Suppress("UNUSED_PARAMETER")
    fun isOkVariableForIndexing(type: ExPhpType, project: Project): Boolean =
            type.getSubkeyByIndex("") != null ||
                    // this is BAD, but in our real code there are lots of untyped arguments with =false default false
                    (type === ExPhpType.BOOL || type === ExPhpType.FALSE)

    @Suppress("UNUSED_PARAMETER")
    fun isOkArrayIndex(type: ExPhpType, project: Project): Boolean = when (type) {
        is ExPhpTypeAny       -> true
        is ExPhpTypePrimitive -> type === ExPhpType.STRING || type === ExPhpType.KMIXED || type === ExPhpType.INT || type === ExPhpType.FLOAT || type === ExPhpType.FALSE || type === ExPhpType.BOOL || type === ExPhpType.NULL
        is ExPhpTypePipe      -> type.items.all { isOkArrayIndex(it, project) } || type.items.any { it === ExPhpType.KMIXED || it === ExPhpType.ANY }
        else                  -> false
    }
}
