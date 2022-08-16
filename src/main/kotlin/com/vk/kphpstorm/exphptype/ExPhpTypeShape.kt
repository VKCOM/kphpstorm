package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * shape(x:int, y:?A, ...) — shape is a list of shape items
 * vararg flag is not stored here: does not influence any behavior
 */
class ExPhpTypeShape(val items: List<ShapeItem>) : ExPhpType {
    class ShapeItem(val keyName: String, val nullable: Boolean, val type: ExPhpType) {
        override fun toString() = "$keyName${if (nullable) "?" else ""}:$type"
        fun toHumanReadable(file: PhpPsiElement) = "$keyName${if (nullable) "?" else ""}:${type.toHumanReadable(file)}"
    }

    override fun toString() = "shape(${items.joinToString(",")})"

    override fun toHumanReadable(expr: PhpPsiElement) = "shape(${items.joinToString { it.toHumanReadable(expr) }})"

    override fun toPhpType(): PhpType {
        val itemsStrJoined = items.joinToString(",") { "${it.keyName}${if (it.nullable) "?" else ""}:${it.type.toPhpType()}" }
        return PhpType().add("shape($itemsStrJoined)")
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        if (indexKey.isEmpty())
            return ExPhpType.ANY
        return items.find { it.keyName == indexKey }?.type
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return ExPhpTypeShape(items.map { ShapeItem(it.keyName, it.nullable, it.type.instantiateTemplate(nameMap)) })
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny   -> true
        is ExPhpTypePipe  -> rhs.isAssignableTo(this, project)
        is ExPhpTypeShape -> true       // any shape is compatible with any other, for simplification (tuples are not)
        else              -> false
    }

    override fun dropForce(): ExPhpType {
        return ExPhpTypeShape(items.mapNotNull { ShapeItem(it.keyName, it.nullable, it.type.dropForce() ?: return@mapNotNull null ) })
    }
}
