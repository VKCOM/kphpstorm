package com.vk.kphpstorm.kphptags.psi.stubs

import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocStubElementTypes
import com.jetbrains.php.lang.psi.stubs.stub_factories.PhpDocTagStubFactory
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeAnyPsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeArrayPsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeCallablePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeForcingPsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeNullablePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypePipePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypePrimitivePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeShapePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeTplInstantiationPsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeTuplePsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocJsonAttributePsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocJsonForEncoderPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTplParameterDeclPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocWarnPerformanceItemPsiImpl

class KphpStubRegistryExtension : StubRegistryExtension {
    override fun register(registry: StubRegistry) {
        registerSerializers(registry)
    }

    private fun registerSerializers(registry: StubRegistry) {
        val docComment = PhpDocStubElementTypes.DOC_COMMENT

        // com.vk.kphpstorm.exphptype.psi
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeAnyPsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeArrayPsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeCallablePsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeForcingPsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeInstancePsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeNullablePsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypePipePsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypePrimitivePsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeShapePsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeTplInstantiationPsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(ExPhpTypeTuplePsiImpl.elementType)
//        )

        // com.vk.kphpstorm.kphptags.psi
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(KphpDocJsonAttributePsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(KphpDocJsonForEncoderPsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(KphpDocTplParameterDeclPsiImpl.elementType)
//        )
//        registry.registerStubFactory(
//            docComment,
//            PhpDocTagStubFactory(KphpDocWarnPerformanceItemPsiImpl.elementType)
//        )
    }
}
