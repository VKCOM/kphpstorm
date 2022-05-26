package com.vk.kphpstorm.inspections.helpers

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.*
import com.vk.kphpstorm.generics.GenericUtil.getGenericPipeType

/**
 * Helps check if something is statically typed
 */
object KphpTypingAnalyzer {
    private val SCALAR_TYPE_HINTS = sortedSetOf(
            "int",
            "float",
            "string",
            "void",
            "bool",
            "false",
            "null",
            "callable",
            "array",
            "object",
            "iterable",
            "resource"
    )

    fun isFunctionStrictlyTyped(function: Function): Boolean {
        // all global functions and methods are strictly typed:
        // must provide @param/hints for all arguments and @return/hint if not void
        // but! with exception to lambdas
        return !function.isClosure
    }

    fun doesFieldHaveType(field: Field): Boolean {
        return !field.declaredType.isEmpty || !field.docType.isEmpty || field.defaultValue != null
    }

    fun isFunctionInferredReturnTypeVoid(function: Function): Boolean {
        return function.inferredType.let { it == PhpType.VOID || it.isEmpty }
    }

    fun isScalarTypeHint(s: String) =
            SCALAR_TYPE_HINTS.contains(s)

    fun doesDocTypeMatchTypeHint(docType: ExPhpType, hintType: ExPhpType, project: Project): Boolean {
        if (docType is ExPhpTypePipe) {
            val genericType = docType.getGenericPipeType()
            if (genericType != null) {
                return doesDocTypeMatchTypeHint(genericType, hintType, project)
            }
        }

        return docType !is ExPhpTypePipe
                && hintType.isAssignableFrom(docType, project)
                && docType.isAssignableFrom(hintType, project)
    }

    fun doesDocTypeDuplicateTypeHint(docType: ExPhpType, hintType: ExPhpType): Boolean =
            docType.toString() == hintType.toString()

    fun canMoveToTypeHint(docType: ExPhpType): Boolean =
            docType is ExPhpTypePrimitive && isScalarTypeHint(docType.typeStr)
                    || docType === ExPhpType.ARRAY_OF_ANY
                    || docType is ExPhpTypeInstance
                    || docType is ExPhpTypeNullable && canMoveToTypeHint(docType.inner)
}
