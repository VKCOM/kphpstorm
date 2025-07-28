package com.vk.kphpstorm.kphptags.psi.stubs

import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.factory.KphpDocTagJsonElementTypeStubFactory
import com.vk.kphpstorm.kphptags.psi.factory.KphpDocTagSimpleElementTypeStubFactory
import com.vk.kphpstorm.kphptags.psi.factory.KphpDocTagTemplateClassElementTypeFactory
import com.vk.kphpstorm.kphptags.psi.factory.KphpDocTagWarnPerformanceElementTypeFactory
import com.vk.kphpstorm.kphptags.psi.serializers.KphpDocTagJsonElementTypeStubSerializer
import com.vk.kphpstorm.kphptags.psi.serializers.KphpDocTagSimpleElementTypeStubSerializer
import com.vk.kphpstorm.kphptags.psi.serializers.KphpDocTagTemplateClassElementTypeSerializer
import com.vk.kphpstorm.kphptags.psi.serializers.KphpDocTagWarnPerformanceElementTypeSerializer

@Suppress("UnstableApiUsage")
class KphpStubRegistryExtension : StubRegistryExtension {
    override fun register(registry: StubRegistry) {
        registerSerializers(registry)
        registerFactories(registry)
    }

    private fun registerSerializers(registry: StubRegistry) {
        registry.registerStubSerializer(
            KphpDocElementTypes.kphpDocTagSimple,
            KphpDocTagSimpleElementTypeStubSerializer()
        )

        registry.registerStubSerializer(
            KphpDocElementTypes.kphpDocTagJson,
            KphpDocTagJsonElementTypeStubSerializer()
        )

        registry.registerStubSerializer(
            KphpDocElementTypes.kphpDocTagTemplateClass,
            KphpDocTagTemplateClassElementTypeSerializer()
        )

        registry.registerStubSerializer(
            KphpDocElementTypes.kphpDocTagWarnPerformance,
            KphpDocTagWarnPerformanceElementTypeSerializer()
        )
    }

    private fun registerFactories(registry: StubRegistry) {
        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagSimple,
            KphpDocTagSimpleElementTypeStubFactory
        )

        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagJson,
            KphpDocTagJsonElementTypeStubFactory
        )

        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagTemplateClass,
            KphpDocTagTemplateClassElementTypeFactory
        )

        registry.registerStubFactory(
            KphpDocElementTypes.kphpDocTagWarnPerformance,
            KphpDocTagWarnPerformanceElementTypeFactory
        )
    }
}
