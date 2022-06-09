package com.vk.kphpstorm.kphptags.psi

import com.vk.kphpstorm.KphpStormParserDefinition


/**
 * Purpose:
 * We want do manage @kphp-... doc tags separately from regular doc tags.
 * This means, that parsing (from tokens to psi) and implementation of such tags is custom.
 * Parsers override getElementType() to one of these, and they must be handled here:
 * @see KphpStormParserDefinition.createElement
 * Also this object must be referenced as <stubElementTypeHolder> in plugin.xml
 * (to make its properties be created before any other initialization)
 */
object KphpDocElementTypes {

    /**
     * Most of @kphp-... tags
     * * are not customly parsed, their psi is trivial: just name + phpDocValue
     *   (normally, we use text (not psi) to access their arguments and annotate them)
     * * do not store anything in stubs, so their contents is unaccessible from other files
     * Such doc tags are 'simple'
     */
    val kphpDocTagSimple = KphpDocTagSimpleElementType

    /**
     * '@kphp-warn-performance ...' and '@kphp-analyze-performance ...'
     * They don't store stubs, but have a psi parser
     */
    val kphpDocTagWarnPerformance = KphpDocTagWarnPerformanceElementType

    /**
     * '@kphp-generic T1, T2: ExtendsClass, T3 = default'
     * This tag stores "T1,T2:ExtendsClass,T3=default" in stubs and has custom psi for them, therefore is not simple
     */
    val kphpDocTagGeneric = KphpDocTagGenericElementType

    /**
     * '@kphp-inherit ExtendsClass<Type>, ImplementsClass<Type>'
     * This tag stores "ExtendsClass<Type>, ImplementsClass<Type>"
     * in stubs and has custom psi for them, therefore is not simple
     */
    val kphpDocTagInherit = KphpDocTagInheritElementType
}
