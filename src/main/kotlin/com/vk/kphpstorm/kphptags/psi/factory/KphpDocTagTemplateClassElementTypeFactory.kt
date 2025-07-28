package com.vk.kphpstorm.kphptags.psi.factory

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocTagStubImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagTemplateClassPsiImpl

object KphpDocTagTemplateClassElementTypeFactory : StubElementFactory<PhpDocTagStub, PhpDocTag> {
    override fun createPsi(stub: PhpDocTagStub): PhpDocTag {
        return KphpDocTagTemplateClassPsiImpl(stub, KphpDocElementTypes.kphpDocTagTemplateClass)
    }

    override fun createStub(psi: PhpDocTag, parentStub: StubElement<out PsiElement>?): PhpDocTagStub {
        // stub value is 'T1,T2' — without spaces
        val stubValue = (psi as KphpDocTagTemplateClassPsiImpl).getTemplateArguments().joinToString(",")
        return KphpDocTagStubImpl(parentStub, KphpDocElementTypes.kphpDocTagTemplateClass, psi.name, stubValue)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean =
        node.elementType == KphpDocElementTypes.kphpDocTagTemplateClass
}
