package com.vk.kphpstorm.kphptags.psi.factory

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagSimplePsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagStubImpl

object KphpDocTagSimpleElementTypeStubFactory : StubElementFactory<PhpDocTagStub, PhpDocTag> {
    override fun shouldCreateStub(node: ASTNode): Boolean =
        node.elementType == KphpDocElementTypes.kphpDocTagSimple

    override fun createStub(
        psi: PhpDocTag,
        parentStub: StubElement<out PsiElement>?
    ): PhpDocTagStub =
        KphpDocTagStubImpl(parentStub, KphpDocElementTypes.kphpDocTagSimple, psi.name, null)

    override fun createPsi(stub: PhpDocTagStub): PhpDocTag =
        KphpDocTagSimplePsiImpl(stub, KphpDocElementTypes.kphpDocTagSimple)
}
