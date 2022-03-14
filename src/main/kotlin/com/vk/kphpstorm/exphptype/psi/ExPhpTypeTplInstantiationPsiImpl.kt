package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.KphpPrimitiveTypes
import com.vk.kphpstorm.helpers.toStringAsNested

/**
 * Wrapper<int>, Container<int[]> — psi is PhpDocType<PhpDocType, ...>
 * PhpType is "Wrapper"|"Wrapper<int>"
 * Important!
 * See shapes and tuples for explanation of this approach.
 */
class ExPhpTypeTplInstantiationPsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeTplInstantiation")
    }

    override fun getNameNode(): ASTNode? = null

    override fun getType(): PhpType {
        // kphp as built-in future<T> and future_queue<T>, which are ints a runtime and in php code, not classes
        val genericClassName = (firstChild as? PhpDocType)?.type?.types?.firstOrNull() ?: return PhpType.EMPTY

        if (genericClassName == "future")
            return PhpType.INT
        if (genericClassName == "future_queue")
            return KphpPrimitiveTypes.PHP_TYPE_ARRAY_OF_ANY

        var innerTypesStr = ""
        var child = firstChild.nextSibling
        while (child != null) {
            if (child is PhpDocType) {
                if (innerTypesStr.length > 1)
                    innerTypesStr += ','
                innerTypesStr += child.type.toStringAsNested()
            }
            child = child.nextSibling
        }

        return PhpType().add(genericClassName).add("$genericClassName<$innerTypesStr>")
    }
}
