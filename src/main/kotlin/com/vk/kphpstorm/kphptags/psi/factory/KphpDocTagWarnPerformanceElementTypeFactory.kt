package com.vk.kphpstorm.kphptags.psi.factory

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagStubImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagWarnPerformancePsiImpl

object KphpDocTagWarnPerformanceElementTypeFactory : StubElementFactory<PhpDocTagStub, PhpDocTag> {
    override fun shouldCreateStub(node: ASTNode): Boolean =
        node.elementType == KphpDocElementTypes.kphpDocTagWarnPerformance


    override fun createStub(psi: PhpDocTag, parentStub: StubElement<out PsiElement>?): PhpDocTagStub{
        return KphpDocTagStubImpl(parentStub, KphpDocElementTypes.kphpDocTagWarnPerformance, psi.name, null)
    }

    override fun createPsi(stub: PhpDocTagStub): PhpDocTag? {
        return KphpDocTagWarnPerformancePsiImpl(stub, KphpDocElementTypes.kphpDocTagWarnPerformance)
    }
}
