package com.vk.kphpstorm.kphptags.psi.factory

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.serializers.KphpDocTagWarnPerformanceElementTypeSerializer

object KphpDocTagWarnPerformanceElementTypeFactory : StubElementFactory<PhpDocTagStub, PhpDocTag> {
    override fun shouldCreateStub(node: ASTNode): Boolean =
        node.elementType == KphpDocElementTypes.kphpDocTagWarnPerformance


    override fun createStub(psi: PhpDocTag, parentStub: StubElement<out PsiElement>?): PhpDocTagStub{
        return KphpDocTagWarnPerformanceElementTypeSerializer().createStub(psi, parentStub)
    }

    override fun createPsi(stub: PhpDocTagStub): PhpDocTag? {
        return KphpDocTagWarnPerformanceElementTypeSerializer().createPsi(stub)
    }
}
