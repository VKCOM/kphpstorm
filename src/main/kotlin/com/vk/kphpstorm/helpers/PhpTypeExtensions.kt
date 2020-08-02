package com.vk.kphpstorm.helpers

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.PhpTypeToExPhpTypeParsing

/**
 * PhpType().add("int").toString() produces "\\int", we often want "int" just for beauty.
 * Keys the same as const vals in [com.vk.kphpstorm.exphptype.KphpPrimitiveTypes]
 */
private val phpTypePrimitiveSlashed = sortedMapOf(
        "\\int" to "int",
        "\\float" to "float",
        "\\string" to "string",
        "\\bool" to "bool",
        "\\false" to "false",
        "\\null" to "null",
        "\\object" to "object",
        "\\array" to "array",
        "\\callable" to "callable",
        "\\void" to "void"
)

/**
 * PhpStorm internals use '|' to split type string to separate types.
 * We want "tuple(int|false, A)" be a single type, so use '/' instead of '|' for pipe separation.
 * (it's not ugly, it's ok: think of it like a custom type format)
 * My parsing from string handles both '|' and '/' [com.vk.kphpstorm.exphptype.PhpTypeToExPhpTypeParsing]
 */
fun PhpType.toStringAsNested(separator: String = "/"): String =
        when (types.size) {
            1    -> types.first().let { phpTypePrimitiveSlashed[it] ?: it }
            else -> types.joinToString(separator) { phpTypePrimitiveSlashed[it] ?: it }
        }

fun PhpType.toExPhpType(): ExPhpType? =
        PhpTypeToExPhpTypeParsing.parse(this)

fun PhpType.toExPhpType(project: Project): ExPhpType? =
        PhpTypeToExPhpTypeParsing.parse(this.global(project))
