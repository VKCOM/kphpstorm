package com.vk.kphpstorm.kphptags.psi

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * When @kphp-... tags (regardless of elementType) are stored as stubs,
 * they hold only name and optional arbitrary string value.
 * Similar to [com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubImpl]
 *
 * TODO: make a research to check are we really need stubs?
 */
class KphpDocTagStubImpl(
    parent: StubElement<*>?,
    elementType: IElementType,
    private val name: String,
    private val value: String?
) : StubBase<PhpDocTag>(parent, elementType), PhpDocTagStub {

    fun getType() = PhpType()
    override fun getName() = name
    override fun getValue() = value

    override fun toString(): String = "$name${if (value != null) ": $value" else ""}"
}
