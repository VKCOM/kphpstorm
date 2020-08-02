package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * T[] — psi is PhpDocType[]
 * PhpType is pluralize() of T
 * (though it is not formally correct, i.e. (int|false)[] != int[]|false[], but let it be)
 */
class ExPhpTypeArrayPsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeArray")
    }

    override fun getNameNode(): ASTNode? = null

    override fun getType(): PhpType {
        return (firstPsiChild as? PhpDocType)?.type?.pluralise() ?: PhpType.EMPTY
    }
}
