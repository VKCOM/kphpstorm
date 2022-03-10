package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocTagSimplePsiImpl

/**
 * 'A', 'asdf\Instance', '\VK\Memcache' — instances (not primitives!) — psi is just PhpDocType
 * PhpType is resolving relative to absolute path, like '\A'
 */
class ExPhpTypeInstancePsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeInstance")
    }

    override fun getType(): PhpType {
        // for "future" don't invoke getType(), because it will be treated as relative class name in namespace
        if (isKphpBuiltinClass())
            return PhpType().add(text)

        // именно здесь, имея "T", мы отличаем — это class T (тогда так и оставляем) или genericsT (пишем %T в PhpType)
        // для этого идём от this по psi-дереву вверх и ищем @kphp-template
        // это наброски, чтобы показать концепцию
        var parent = this.parent
        while (parent !is PsiFile) {
            // например, когда мы внутри @param T, то когда-то дойдём до doc comment, где написано @kphp-template
            if (parent is PhpDocComment) {
                val doc = parent
                for (child in doc.children) {
                    if (child is KphpDocTagSimplePsiImpl) {
                        // понятное дело, что @kphp-template не нужно парсить из строки, оно должно превращаться в другое psi-дерево
                        if (child.name == "@kphp-template" && child.text == "@kphp-template T") {
                            return PhpType().add("%$text")
                        }
                    }
                }
            }
            // например, когда мы внутри @var T, то когда дойдём до функции/класса вверх, @kphp-template будет в нём
            if (parent is Function) {
                val doc = PsiTreeUtil.skipWhitespacesBackward(parent) as? PhpDocComment
                if (doc != null) {
                    for (child in doc.children) {
                        if (child is KphpDocTagSimplePsiImpl) {
                            if (child.name == "@kphp-template" && child.text == "@kphp-template T") {
                                return PhpType().add("%$text")
                            }
                        }
                    }
                }
            }
            parent = parent.parent
        }

        // todo не использовать getType!!! это приведёт к повторному парсингу дерева вверх, до use'ов
        // иными словами, конечная цель — код getType частично скопировать, реализовав свой резолвинг классов,
        // попутно детектя @kphp-template по пути вверх
        return getType(this, text)
    }

    fun isKphpBuiltinClass() = text.let {
        it == "future" || it == "future_queue" || it == "\\RpcConnection"
    }
}
