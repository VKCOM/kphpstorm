package com.vk.kphpstorm.kphptags.psi.stubs

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import com.intellij.util.io.StringRef
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagTemplateClassPsiImpl

@Suppress("UnstableApiUsage")
object KphpDocTagTemplateClassElementTypeFactory : StubElementFactory<PhpDocTagStub, KphpDocTagTemplateClassPsiImpl> {
    override fun createPsi(stub: PhpDocTagStub): KphpDocTagTemplateClassPsiImpl {
        return KphpDocTagTemplateClassPsiImpl(stub, KphpDocElementTypes.kphpDocTagTemplateClass)
    }

    override fun createStub(psi: KphpDocTagTemplateClassPsiImpl, parentStub: StubElement<out PsiElement>?): PhpDocTagStub {
        // stub value is 'T1,T2' — without spaces
        val stubValue = psi.getTemplateArguments().joinToString(",")

        return PhpDocTagStubImpl(
            parentStub,
            KphpDocElementTypes.kphpDocTagTemplateClass,
            StringRef.fromString(psi.name),
            StringRef.fromString(stubValue)
        )
    }

    override fun shouldCreateStub(node: ASTNode): Boolean =
        node.elementType == KphpDocElementTypes.kphpDocTagTemplateClass
}
