package com.vk.kphpstorm.exphptype

import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.KphpPrimitiveTypes.mapPrimitiveToPhpType

/**
 * Here we define how kphp primitives map to PhpType and which primitives can be assigned to which.
 * [mapPrimitiveToPhpType] defines which tokens are primitives (other are instances)
 * Important! 'mixed' in phpdoc is treated as 'kmixed'.
 * So, 'mixed' PhpType can be emerged only by PhpStorm internal inferring and is considered as any.
 */
object KphpPrimitiveTypes {
    val PHP_TYPE_ANY = PhpType.PhpTypeBuilder().add("\\any").build()
    val PHP_TYPE_KMIXED = PhpType.PhpTypeBuilder().add("\\kmixed").build()
    val PHP_TYPE_ARRAY_OF_ANY = PhpType.PhpTypeBuilder().add("\\any[]").build()

    const val INT = "int"
    const val FLOAT = "float"
    const val STRING = "string"
    const val BOOL = "bool"
    const val FALSE = "false"
    const val NULL = "null"
    const val OBJECT = "object"
    const val CALLABLE = "callable"
    const val VOID = "void"
    // not to be messed up with native PhpStorm "mixed", KPHP "mixed" is called "kmixed" everywhere
    const val KMIXED = "kmixed"
    // "array" is not a primitive type: it is "any[]"

    /**
     * Map how phpdoc string maps to PhpType.
     * Synonyms like 'boolean' are also listed here.
     * These and only there keys are assumed to be [com.vk.kphpstorm.exphptype.psi.ExPhpTypePrimitivePsiImpl]
     * Other strings in phpdoc will be treated as  [com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl]
     */
    val mapPrimitiveToPhpType = sortedMapOf(
            "int" to PhpType.INT,
            "float" to PhpType.FLOAT,
            "double" to PhpType.FLOAT,
            "string" to PhpType.STRING,
            "bool" to PhpType.BOOLEAN,
            "boolean" to PhpType.BOOLEAN,
            "true" to PhpType.BOOLEAN,
            "false" to PhpType.FALSE,
            "null" to PhpType.NULL,
            "object" to PhpType.OBJECT,
            "callable" to PhpType.CALLABLE,
            "Closure" to PhpType.CALLABLE,
            "void" to PhpType.VOID,
            "resource" to PhpType.INT,
            "kmixed" to PHP_TYPE_KMIXED,
            "any" to PHP_TYPE_ANY,
            // important! "mixed" in phpdoc will be "kmixed" in PhpType / ExPhpType.KMIXED
            // important! but if "Set up for Project" not done — plain PHP project — still "mixed", see ExPhpTypePrimitivePsiImpl
            "mixed" to PHP_TYPE_KMIXED,
            // important! "array" in phpdoc will be "any[]" in PhpType / ExPhpTypeArray(ExPhpTypeAny)
            "array" to PHP_TYPE_ARRAY_OF_ANY
    )
}
