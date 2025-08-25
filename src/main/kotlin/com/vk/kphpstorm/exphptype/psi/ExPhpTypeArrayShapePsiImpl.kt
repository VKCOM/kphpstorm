package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.util.elementType
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.helpers.toStringAsNested

class ExPhpTypeArrayShapePsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeArrayShape")
    }

    override fun getNameNode(): ASTNode? = null

    override fun getType(): PhpType {
        var itemsStr = ""
        var child = firstChild?.nextSibling?.nextSibling    // after '('
        while (child != null) {
            // key name
            if (child.elementType == PhpDocTokenTypes.DOC_IDENTIFIER || child.elementType == PhpDocTokenTypes.DOC_STRING) {
                if (itemsStr.length > 1)
                    itemsStr += ','
                itemsStr += child.text
                if (child.nextSibling?.text?.let { it.isNotEmpty() && it[0] == '?' } == true)     // nullable
                    itemsStr += '?'
                itemsStr += ':'
            }
            // key type
            if (child is PhpDocType)
                itemsStr += child.type.toStringAsNested()

            child = child.nextSibling
        }
        // vararg shapes with "..." in the end are not reflected in PhpType/ExPhpType 

        return PhpType().add("array{$itemsStr}")
    }
}
