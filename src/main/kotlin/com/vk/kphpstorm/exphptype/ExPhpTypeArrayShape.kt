package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * shape(x:int, y:?A, ...) — shape is a list of shape items
 * vararg flag is not stored here: does not influence any behavior
 */
// TODO: generalize ShapeItem for both ArrayShape and regular Shape
class ExPhpTypeArrayShape(val items: List<ExPhpTypeShape.ShapeItem>) : ExPhpType {
    override fun toString() = "array{${items.joinToString(",")}}"

    override fun toHumanReadable(expr: PhpPsiElement) = "array{${items.joinToString { it.toHumanReadable(expr) }}}"

    override fun toPhpType(): PhpType {
        val itemsStrJoined =
            items.joinToString(",") { "${it.keyName}${if (it.nullable) "?" else ""}:${it.type.toPhpType()}" }
        return PhpType().add("array{$itemsStrJoined}")
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        if (indexKey.isEmpty())
            return ExPhpType.ANY

        return items.find { it.keyName == indexKey }?.type
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return ExPhpTypeArrayShape(items.map {
            ExPhpTypeShape.ShapeItem(
                it.keyName,
                it.nullable,
                it.type.instantiateTemplate(nameMap)
            )
        })
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny -> true
        is ExPhpTypePipe -> rhs.isAssignableTo(this, project)
        is ExPhpTypeArrayShape -> true // any array shape is compatible with any other, for simplification (tuples are not)
        else -> false
    }
}
