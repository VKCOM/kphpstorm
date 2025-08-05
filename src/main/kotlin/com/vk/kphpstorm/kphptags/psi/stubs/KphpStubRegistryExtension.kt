package com.vk.kphpstorm.kphptags.psi.stubs

import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubSerializer
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

        registry.registerStubSerializer(
            KphpDocElementTypes.kphpDocTagWarnPerformance,
            PhpDocTagStubSerializer(KphpDocElementTypes.kphpDocTagWarnPerformance)
        )

        registry.registerStubSerializer(
            KphpDocElementTypes.kphpDocTagJson,
            PhpDocTagStubSerializer(KphpDocElementTypes.kphpDocTagJson)
        )
    }

    private fun registerFactories(registry: StubRegistry) {
        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagSimple,
            KphpDocTagStubFactory(KphpDocElementTypes.kphpDocTagSimple)
        )

        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagTemplateClass,
            KphpDocTagTemplateClassElementTypeFactory
        )

        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagWarnPerformance,
            KphpDocTagStubFactory(KphpDocElementTypes.kphpDocTagWarnPerformance)
        )

        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagJson,
            KphpDocTagStubFactory(KphpDocElementTypes.kphpDocTagJson)
        )
    }
}
