package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub

class KphpDocTagJsonPsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
    constructor(stub: PhpDocTagStub, nodeType: IElementType) : super(stub, nodeType)

    fun item(): KphpDocJsonAttributePsiImpl? {
        val item = PsiTreeUtil.skipWhitespacesForward(firstChild)
        val psiElement = if (item is KphpDocJsonForEncoderPsiImpl) {
            PsiTreeUtil.skipWhitespacesForward(item)
        } else {
            item
        }

        return psiElement as? KphpDocJsonAttributePsiImpl
    }

    fun forElement(): KphpDocJsonForEncoderPsiImpl? {
        val item = PsiTreeUtil.skipWhitespacesForward(firstChild)

        return item as? KphpDocJsonForEncoderPsiImpl
    }
}
