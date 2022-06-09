package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub

/**
 * Implementation of '@kphp-inherit' tag — created either from stub or from ast
 * @see KphpDocElementTypes.kphpDocTagGeneric
 */
class KphpDocTagInheritPsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
    constructor(stub: PhpDocTagStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun types(): List<KphpDocInheritParameterDeclPsiImpl> {
        return children.filterIsInstance<KphpDocInheritParameterDeclPsiImpl>()
    }
}
