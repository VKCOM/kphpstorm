package com.vk.kphpstorm.kphptags.psi

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * When @kphp-... tags (regardless of elementType) are stored as stubs,
 * they hold only name and optional arbitrary string value.
 * Similar to [com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStubImpl]
 */
class KphpDocTagStubImpl(
        parent: StubElement<*>?,
        elementType: IStubElementType<*, *>,
        private val name: String,
        private val value: String?
) : StubBase<PhpDocTag>(parent, elementType), PhpDocTagStub {

    override fun getType() = PhpType()
    override fun getName() = name
    override fun getValue() = value
}
