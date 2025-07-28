package com.vk.kphpstorm.kphptags.psi.stubs

import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubSerializer
import com.jetbrains.php.lang.psi.stubs.stub_factories.PhpDocTagStubFactory
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes

@Suppress("UnstableApiUsage")
class KphpStubRegistryExtension : StubRegistryExtension {
    override fun register(registry: StubRegistry) {
        registerSerializers(registry)
        registerFactories(registry)
    }

    private fun registerSerializers(registry: StubRegistry) {
        registry.registerStubSerializer(
            KphpDocElementTypes.kphpDocTagSimple,
            PhpDocTagStubSerializer(KphpDocElementTypes.kphpDocTagSimple)
        )

        registry.registerStubSerializer(
            KphpDocElementTypes.kphpDocTagTemplateClass,
            PhpDocTagStubSerializer(KphpDocElementTypes.kphpDocTagTemplateClass)
        )
    }

    private fun registerFactories(registry: StubRegistry) {
        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagSimple,
            PhpDocTagStubFactory(KphpDocElementTypes.kphpDocTagSimple)
        )

        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagTemplateClass,
            KphpDocTagTemplateClassElementTypeFactory
        )
    }
}
