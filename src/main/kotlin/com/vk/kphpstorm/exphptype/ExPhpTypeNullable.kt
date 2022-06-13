package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * ?int and so on supported in phpdocs - just like type hints. ?int[] is ?(int[])
 */
class ExPhpTypeNullable(val inner: ExPhpType) : ExPhpType {
    override fun toString() = "?$inner"

    override fun toHumanReadable(expr: PhpPsiElement) = "?" + inner.toHumanReadable(expr)

    override fun toPhpType(): PhpType {
        return PhpType().add(inner.toPhpType()).add("null")
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return inner.getSubkeyByIndex(indexKey)
    }

    override fun instantiateGeneric(nameMap: Map<String, ExPhpType>): ExPhpType {
        return ExPhpTypeNullable(inner.instantiateGeneric(nameMap))
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny       -> true
        is ExPhpTypePipe      -> rhs.isAssignableTo(this, project)
        is ExPhpTypeNullable  -> inner.isAssignableFrom(rhs.inner, project)
        is ExPhpTypePrimitive -> rhs === ExPhpType.NULL || inner.isAssignableFrom(rhs, project)
        else                  -> inner.isAssignableFrom(rhs, project)
    }
}
