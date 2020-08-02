package com.vk.kphpstorm.kphptags.psi

import com.jetbrains.php.lang.documentation.phpdoc.parser.tags.PhpDocTagParser

/**
 * All available element types from [KphpDocElementTypes] must implement this interface
 */
interface KphpDocTagElementType {
    fun getTagParser(): PhpDocTagParser
}
