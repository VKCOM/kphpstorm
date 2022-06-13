package com.vk.kphpstorm.highlighting

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.psi.codeStyle.DisplayPriority
import com.intellij.psi.codeStyle.DisplayPrioritySortable
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.highlighter.PhpColorPageHighlighter
import com.jetbrains.php.lang.highlighter.PhpHighlightingData

/**
 * Settings > Editor > Color Scheme > KPHP
 */
class KphpColorsAndFontsPage : ColorSettingsPage, DisplayPrioritySortable {
    private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("phpdoc: @kphp-... tag", KphpHighlightingData.PHPDOC_TAG_KPHP),
            AttributesDescriptor("phpdoc: other tags (@param / @see / etc)", KphpHighlightingData.PHPDOC_TAG_REGULAR),
            AttributesDescriptor("phpdoc: type inside @var / @param / @return", KphpHighlightingData.PHPDOC_TYPE_INSIDE),
            AttributesDescriptor("phpdoc: variable of @param", PhpHighlightingData.DOC_PARAMETER),
            AttributesDescriptor("phpdoc: generic T names", KphpHighlightingData.PHPDOC_GENERIC_TYPE_T),
            AttributesDescriptor("function call: php predefined (array_pop / ini_get / etc)", PhpHighlightingData.PREDEFINED_SYMBOL),
            AttributesDescriptor("function call: kphp native (wait / instance_cast / etc)", KphpHighlightingData.FUNC_CALL_KPHP_NATIVE),
            AttributesDescriptor("function call: regular (not instance)", KphpHighlightingData.FUNC_CALL_REGULAR),
    )

    override fun getHighlighter() = PhpColorPageHighlighter(mapOf())

    override fun getAdditionalHighlightingTagToDescriptorMap() = mutableMapOf(
            "bg" to PhpHighlightingData.PHP_SCRIPTING,
            "kw" to PhpHighlightingData.KEYWORD,

            "pdtag_kphp" to KphpHighlightingData.PHPDOC_TAG_KPHP,
            "pdtag" to KphpHighlightingData.PHPDOC_TAG_REGULAR,
            "pd_type" to KphpHighlightingData.PHPDOC_TYPE_INSIDE,
            "pd_var" to PhpHighlightingData.DOC_PARAMETER,

            "f_reg" to KphpHighlightingData.FUNC_CALL_REGULAR,
            "f_php" to PhpHighlightingData.PREDEFINED_SYMBOL,
            "f_native" to KphpHighlightingData.FUNC_CALL_KPHP_NATIVE,

            "f_generic_t" to KphpHighlightingData.PHPDOC_TYPE_INSIDE,
            "f_generic_extends" to PhpHighlightingData.DOC_COMMENT,
    )

    override fun getIcon() = PhpFileType.INSTANCE.icon

    override fun getAttributeDescriptors() = DESCRIPTORS

    override fun getColorDescriptors() = arrayOf<ColorDescriptor>()

    override fun getDisplayName() = "KPHP"

    override fun getDemoText() = """
        <?php
        
        /**
         * <bg><pdtag_kphp>@kphp-immutable-class</pdtag_kphp></bg>
         */
        class User {
          /** <bg><pdtag>@var</pdtag></bg> <pd_type>string | false</pd_type> */
          public ${'$'}name = false;   
        }
        
        /**
         * <bg><pdtag_kphp>@kphp-infer</pdtag_kphp></bg>
         * <bg><pdtag>@param</pdtag></bg> <pd_type>tuple(int, A) | null</pd_type> <bg><pd_var>${'$'}arg</pd_var></bg>
         * <bg><pdtag>@return</pdtag></bg> <pd_type>tuple(int, string)</pd_type>
         */
        function demo_tuple(${'$'}arg) {
          return <bg><kw>tuple</kw></bg>(${'$'}arg[0], 'str');
        }

        <bg><kw>fork</kw></bg>(<bg><f_reg>demo_tuple</f_reg></bg>(<bg><kw>tuple</kw></bg>(1, new User)));
        <bg><f_native>instance_to_array</f_native></bg>(${'$'}user);
        <bg><f_php>ini_get</f_php></bg>('memory_limit');

        /**
         * <bg><pdtag_kphp>@kphp-generic</pdtag_kphp></bg> <bg><f_generic_t>T</f_generic_t></bg>: <bg><f_generic_extends>Field<int></f_generic_extends></bg>
         */
        function demo_generic(): bool {
          return true;
        }

    """.trimIndent()

    override fun getPriority() = DisplayPriority.KEY_LANGUAGE_SETTINGS

}
