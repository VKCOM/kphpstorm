package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.KphpPrimitiveTypes

/**
 * ANY is a special type which can be assigned to anything, and anything can be assigned to it.
 * 'any' in phpdoc (case sensitive).
 * PhpType is 'any' — but it's not a class reference, it's handled as separate ExPhpType.
 * For instance, "array" in phpdoc is any[].
 * Note! "mixed" in phpdoc is "var", that's why "mixed" can be emerged only by PhpStorm inferring and is mapped to any.
 */
class ExPhpTypeAnyPsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeAny")
    }

    override fun getNameNode(): ASTNode? = null

    override fun getType(): PhpType {
        return KphpPrimitiveTypes.PHP_TYPE_ANY
    }
}
