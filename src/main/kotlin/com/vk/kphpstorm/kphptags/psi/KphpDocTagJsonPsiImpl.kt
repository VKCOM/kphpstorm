package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub

class KphpDocTagJsonPsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
    constructor(stub: PhpDocTagStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun item(): KphpDocJsonPropertyPsiImpl? {
        val item = PsiTreeUtil.skipWhitespacesForward(firstChild)
        val psiElement = if (item is KphpDocJsonForEncoderPsiImpl) {
            PsiTreeUtil.skipWhitespacesForward(item)
        } else {
            item
        }

        return psiElement as? KphpDocJsonPropertyPsiImpl
    }

    fun forElement(): KphpDocJsonForEncoderPsiImpl? {
        val item = PsiTreeUtil.skipWhitespacesForward(firstChild)

        return item as? KphpDocJsonForEncoderPsiImpl
    }
}
