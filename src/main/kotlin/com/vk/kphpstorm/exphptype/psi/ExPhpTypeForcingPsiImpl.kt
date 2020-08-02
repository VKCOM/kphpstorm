package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.helpers.toStringAsNested

class ExPhpTypeForcingPsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeForcing")
    }

    override fun getNameNode(): ASTNode? = null

    override fun getType(): PhpType {
        val child = firstPsiChild as? PhpDocType ?: return PhpType.EMPTY
        return PhpType().add("force(${child.type.toStringAsNested()})")
    }
}
