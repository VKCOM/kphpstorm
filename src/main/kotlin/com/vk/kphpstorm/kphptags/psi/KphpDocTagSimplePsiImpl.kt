package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl

/**
 * Implemetation of 'simple' @kphp-... tag — created either from stub or from ast
 * @see KphpDocElementTypes.kphpDocTagSimple
 */
class KphpDocTagSimplePsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
}
