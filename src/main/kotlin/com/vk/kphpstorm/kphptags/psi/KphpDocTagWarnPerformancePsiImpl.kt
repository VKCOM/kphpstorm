package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub

/**
 * Implemetation of '@kphp-analyze-performance' and '@kphp-warn-performance' tags
 * @see KphpDocElementTypes.kphpDocTagWarnPerformance
 */
class KphpDocTagWarnPerformancePsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
    constructor(stub: PhpDocTagStub, nodeType: IElementType) : super(stub, nodeType)
}
