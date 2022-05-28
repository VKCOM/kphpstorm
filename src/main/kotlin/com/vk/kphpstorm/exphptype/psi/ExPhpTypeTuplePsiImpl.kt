package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.helpers.toStringAsNested

/**
 * tuple(int, A|null) — psi is tuple(PhpDocType, PhpDocType, ...)
 * PhpType is "tuple(int,A/null)"
 * Important!
 * Inner pipes inside "tuple(...)" are separated with '/' not '|' — because PhpStorm does not know anything
 * about our nested subtypes and often splits a whole string using '|'. So, '/' to avoid this splitting.
 */
class ExPhpTypeTuplePsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeTuple")
    }

    override fun getNameNode(): ASTNode? = null

    override fun getType(): PhpType {
        var itemsStr = ""
        var child = firstChild
        while (child != null) {
            if (child is PhpDocType) {
                if (itemsStr.length > 1)
                    itemsStr += ','
                // TODO: подумать
                itemsStr += child.type.global(project).toStringAsNested()
            }
            child = child.nextSibling
        }

        // tuple(...) inside PhpType is better than tuple<...> because in PhpStorm hovers rendering
        return PhpType().add("tuple($itemsStr)")
    }
}
