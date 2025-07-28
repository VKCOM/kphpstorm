package com.vk.kphpstorm.kphptags.psi.factory

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagJsonPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagStubImpl

object KphpDocTagJsonElementTypeStubFactory : StubElementFactory<PhpDocTagStub, PhpDocTag> {
    override fun shouldCreateStub(node: ASTNode): Boolean =
        node.elementType == KphpDocElementTypes.kphpDocTagJson

    override fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagJsonPsiImpl(stub, KphpDocElementTypes.kphpDocTagJson)
    }

    override fun createStub(psi: PhpDocTag, parentStub: StubElement<*>?): PhpDocTagStub {
        return KphpDocTagStubImpl(parentStub, KphpDocElementTypes.kphpDocTagJson, psi.name, null)
    }
}
