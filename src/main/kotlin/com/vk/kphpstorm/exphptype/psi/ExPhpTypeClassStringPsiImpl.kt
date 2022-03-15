package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.KphpPrimitiveTypes
import com.vk.kphpstorm.generics.GenericUtil

/**
 * class-string<Foo> — psi is class-string(Foo) corresponding type of Foo::class
 * PhpType is "class-string(Foo)"
 */
class ExPhpTypeClassStringPsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeClassString")
    }

    private val className: String

    init {
        val brace = if (text.contains('(')) listOf('(', ')') else listOf('<', '>')
        // Во время написания типа если он уже завершен.
        className = if (text.contains(brace[0]) && text.contains(brace[1])) {
            val genericType = text.substring(text.indexOf(brace[0]) + 1 until text.indexOf(brace[1]))

            // В случае когда класс на самом деле является шаблонным типом нам нужно мимикрировать тип
            // и добавить знак процента к имени типа, чтобы в дальнейшем работать с ним как с шаблоном.
            val genericMark = if (GenericUtil.nameIsGeneric(this, genericType)) "%" else ""

            "$genericMark$genericType"
        } else {
            ""
        }
    }

    override fun getNameNode(): ASTNode? = null

    override fun getType(): PhpType {
        if (className.isEmpty()) return KphpPrimitiveTypes.PHP_TYPE_ANY
        return PhpType().add("class-string($className)")
    }
}
