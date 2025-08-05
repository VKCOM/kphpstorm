package com.vk.kphpstorm.kphptags.psi.stubs

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import com.intellij.psi.tree.IElementType
import com.intellij.util.io.StringRef
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubImpl

@Suppress("UnstableApiUsage")
class KphpDocTagStubFactory(private val elementType: IElementType) : StubElementFactory<PhpDocTagStub, PhpDocTagImpl> {
    override fun createStub(psi: PhpDocTagImpl, parentStub: StubElement<out PsiElement>?): PhpDocTagStub {
        return PhpDocTagStubImpl(
            parentStub,
            elementType,
            StringRef.fromString(psi.name),
            null
        )
    }

    override fun createPsi(stub: PhpDocTagStub): PhpDocTagImpl {
        return PhpDocTagImpl(stub, stub.elementType)
    }
}
