package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * T1|T2|... — psi is PhpDocType|PhpDocType|...
 * PhpType is just add() of inners, so string representation also uses | as separation
 */
class ExPhpTypePipePsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypePipe")
    }

    override fun getType(): PhpType {
        val type = PhpType()
        var child = firstPsiChild
        while (child != null) {
            if (child is PhpDocType)
                type.add(child.type)
            child = child.nextPsiSibling
        }
        return type
    }
}
