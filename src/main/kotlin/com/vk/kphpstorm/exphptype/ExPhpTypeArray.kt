package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * Array is T[], T can be complex type: (int|false)[] is ok. T[][] is array of arrays.
 * "array" as primitive is any[]
 */
class ExPhpTypeArray(val inner: ExPhpType) : ExPhpType {
    override fun toString() = "$inner[]"

    override fun toHumanReadable(expr: PhpPsiElement) =
            if (inner === ExPhpType.ANY) "array"
            else "${inner.toHumanReadable(expr)}[]"

    override fun toPhpType(): PhpType {
        return PhpType().add(inner.toPhpType()).pluralise()
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return inner
    }

    override fun instantiateGeneric(nameMap: Map<String, ExPhpType>): ExPhpType {
        return ExPhpTypeArray(inner.instantiateGeneric(nameMap))
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean {
        return when (rhs) {
            is ExPhpTypeAny   -> true
            is ExPhpTypePipe  -> rhs.isAssignableTo(this, project)
            is ExPhpTypeArray -> inner.isAssignableFrom(rhs.inner, project)
            else              -> false
        }
    }
}
