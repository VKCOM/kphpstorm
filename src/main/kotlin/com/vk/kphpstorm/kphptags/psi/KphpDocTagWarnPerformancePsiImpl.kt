package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl

/**
 * Implemetation of '@kphp-analyze-performance' and '@kphp-warn-performance' tags
 * @see KphpDocElementTypes.kphpDocTagWarnPerformance
 */
class KphpDocTagWarnPerformancePsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
}
