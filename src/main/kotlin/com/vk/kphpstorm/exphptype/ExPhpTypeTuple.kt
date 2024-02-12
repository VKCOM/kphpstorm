package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.helpers.toStringAsNested

/**
 * tuple(int, A), tuple(int[], A|null) — tuple is a list of items
 */
class ExPhpTypeTuple(val items: List<ExPhpType>) : ExPhpType {
    override fun toString() = "tuple(${items.joinToString(",")})"

    override fun toHumanReadable(expr: PhpPsiElement) = "tuple(${items.joinToString { it.toHumanReadable(expr) }})"

    override fun toPhpType(): PhpType {
        val typesStrJoined = items.joinToString(",") { it.toPhpType().toStringAsNested() }
        return PhpType().add("tuple($typesStrJoined)")
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        if (indexKey.isEmpty())
            return ExPhpType.ANY
        val idx = indexKey.toIntOrNull() ?: return null
        return if (idx >= 0 && idx < items.size) items[idx] else null
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return ExPhpTypeTuple(items.map { it.instantiateTemplate(nameMap) })
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny      -> true
        is ExPhpTypePipe     -> rhs.isAssignableTo(this, project)
        is ExPhpTypeTuple    -> items.size == rhs.items.size && items.indices.all { items[it].isAssignableFrom(rhs.items[it], project) }
        else                 -> false
    }

    override fun dropForce(): ExPhpType {
        return ExPhpTypeTuple(items.mapNotNull { it.dropForce() })
    }
}
