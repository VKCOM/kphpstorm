package com.vk.kphpstorm.kphptags

/**
 * All known '@kphp-...' doc tags that are highlighted/analyzed by this PhpStorm plugin
 */
val ALL_KPHPDOC_TAGS: List<KphpDocTag> = listOf(
        KphpColorDocTag,
        KphpInlineDocTag,
        KphpInferDocTag,
        KphpPureFunctionDocTag,
        KphpFlattenDocTag,
        KphpRequiredDocTag,
        KphpSyncDocTag,
        KphpReturnDocTag,
        KphpShouldNotThrowDocTag,
        KphpThrowsDocTag,
        KphpDisableWarningsDocTag,
        KphpNoReturnDocTag,
        KphpRuntimeCheckDocTag,
        KphpWarnUnusedResultDocTag,
        KphpProfileDocTag,
        KphpProfileAllowInlineDocTag,
        KphpAnalyzePerformanceDocTag,
        KphpWarnPerformanceDocTag,

        KphpSerializableDocTag,
        KphpReservedFieldsDocTag,
        KphpTemplateClassDocTag,
        KphpGenericDocTag,
        KphpMemcacheClassDocTag,
        KphpImmutableClassDocTag,
        KphpTlClassDocTag,

        KphpSerializedFieldDocTag,
        KphpSerializedFloat32DocTag,
        KphpConstDocTag
)
