package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * Type of special class constant (e.g. `Foo::class`).
 */
class ExPhpTypeClassString(val inner: ExPhpType) : ExPhpType {
    override fun toString() = "class-string($inner)"

    override fun toHumanReadable(expr: PhpPsiElement) = "class-string($inner)"

    override fun equals(other: Any?) = other is ExPhpTypeClassString && inner == other.inner

    override fun hashCode() = 35

    override fun toPhpType() = PhpType().add("class-string(${inner.toPhpType()})")

    override fun getSubkeyByIndex(indexKey: String) = this

    override fun instantiateGeneric(nameMap: Map<String, ExPhpType>): ExPhpType {
        // TODO: подумать тут
        val fqn = when (inner) {
            is ExPhpTypeInstance -> inner.fqn
            is ExPhpTypeGenericsT -> inner.nameT
            else -> ""
        }

        return nameMap[fqn]?.let { ExPhpTypeClassString(ExPhpTypeInstance(it.toString())) } ?: this
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        // class-string<T> is only compatible with class-string<E>
        // if class E is compatible with class T.
        is ExPhpTypeClassString -> inner.isAssignableFrom(rhs.inner, project)
        else                    -> false
    }
}
