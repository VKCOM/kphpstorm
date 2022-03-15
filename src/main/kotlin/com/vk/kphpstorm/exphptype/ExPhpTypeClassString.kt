package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * Type of special class constant (`Foo::class`).
 */
class ExPhpTypeClassString(val inner: ExPhpType) : ExPhpType {
    override fun toString() = "class-string($inner)"

    override fun toHumanReadable(expr: PhpPsiElement) = "class-string($inner)"

    override fun equals(other: Any?) = other is ExPhpTypeClassString && inner == other.inner

    override fun hashCode() = 35

    override fun toPhpType(): PhpType {
        return PhpType().add("class-string($inner)")
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return this
    }

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
        // нативный вывод типов дает тип string|class-string<T> для T::class, поэтому
        // необходимо обработать этот случай отдельно
        is ExPhpTypePipe        -> {
            val containsString = rhs.items.any { it == ExPhpType.STRING }
            if (rhs.items.size == 2 && containsString) {
                val otherType = rhs.items.find { it != ExPhpType.STRING }
                if (otherType == null) false
                else isAssignableFrom(otherType, project)
            } else false
        }
        // class-string<T> совместим только с class-string<E> при условии
        // что класс E является допустимым для класса T.
        is ExPhpTypeClassString -> inner.isAssignableFrom(rhs.inner, project)
        else                    -> false
    }

    companion object {
        // нативный вывод типов дает тип string|class-string<T> для T::class,
        // из-за этого в некоторых местах нужна дополнительная логика.
        fun isNativePipeWithString(pipe: ExPhpTypePipe): Boolean {
            if (pipe.items.size != 2) return false
            val otherType = pipe.items.find { it != ExPhpType.STRING }

            return otherType is ExPhpTypeClassString
        }

        fun getClassFromNativePipeWithString(pipe: ExPhpTypePipe): ExPhpType {
            return pipe.items.find { it != ExPhpType.STRING }!!
        }
    }
}
