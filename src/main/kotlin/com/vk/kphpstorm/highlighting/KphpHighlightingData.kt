package com.vk.kphpstorm.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.jetbrains.php.lang.highlighter.PhpHighlightingData

/**
 * This object contains all highlighting keys that can be configured in
 * Settings > Editor > Color Scheme > KPHP
 * @see KphpColorsAndFontsPage
 * first parameter of createTextAttributesKey() must the the same as referenced in resources/colorSchemes .xml files
 */
object KphpHighlightingData {
    val PHPDOC_TAG_KPHP = TextAttributesKey.createTextAttributesKey("PHPDOC_TAG_KPHP", PhpHighlightingData.DOC_TAG)
    val PHPDOC_TAG_REGULAR = TextAttributesKey.createTextAttributesKey("PHPDOC_TAG_REGULAR", PhpHighlightingData.DOC_TAG)
    val PHPDOC_TYPE_INSIDE = TextAttributesKey.createTextAttributesKey("PHPDOC_TYPE_INSIDE", PhpHighlightingData.DOC_IDENTIFIER)

    val FUNC_CALL_REGULAR = TextAttributesKey.createTextAttributesKey("FUNC_CALL_REGULAR", PhpHighlightingData.FUNCTION_CALL)
    val FUNC_CALL_KPHP_NATIVE = TextAttributesKey.createTextAttributesKey("FUNC_CALL_KPHP_NATIVE", PhpHighlightingData.FUNCTION_CALL)
}
