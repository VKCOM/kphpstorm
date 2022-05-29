package com.vk.kphpstorm.exphptype.psi

import com.intellij.lang.ASTNode
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * callable(int, float):int - psi is callable(PhpType, PhpType):PhpType
 * (return type can be missed, e.g. "callable(int)", void assumed)
 * "callable" as a keyword (not typed callable) is ExPhpTypePrimitive, not this psi
 * PhpType in "callable" (not analyzed deeply by IDE, only in terms of compilation)
 */
class ExPhpTypeCallablePsiImpl(node: ASTNode) : PhpDocTypeImpl(node) {
    companion object {
        val elementType = PhpDocElementType("exPhpTypeCallable")
    }

    override fun getNameNode(): ASTNode? = null

    override fun getType(): PhpType {
        return PhpType.CALLABLE

//        TODO: enable after 2022.2
//        val argTypes = mutableListOf<String>()
//        var returnType = ""
//        var nextReturnType = false
//        var child = firstChild.nextSibling
//        while (child != null) {
//            if (child is PhpDocType) {
//                val type = child.type.toStringAsNested()
//                if (nextReturnType) {
//                    returnType = type
//                    break
//                }
//                argTypes.add(type)
//            }
//            if (child.elementType == PhpDocTokenTypes.DOC_TEXT && child.text == ":") {
//                nextReturnType = true
//            }
//            child = child.nextSibling
//        }
//
//        val argTypesStr = argTypes.joinToString(",")
//        return PhpType().add("callable($argTypesStr):$returnType")
    }
}
