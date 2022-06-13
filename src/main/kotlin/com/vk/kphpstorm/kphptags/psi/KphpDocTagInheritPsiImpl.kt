package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub

/**
 * Implementation of '@kphp-inherit' tag — created either from stub or from ast
 * @see KphpDocElementTypes.kphpDocTagGeneric
 */
class KphpDocTagInheritPsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
    constructor(stub: PhpDocTagStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun getParametersPsi(): List<KphpDocInheritParameterDeclPsiImpl> =
        children.filterIsInstance<KphpDocInheritParameterDeclPsiImpl>()

    fun getParameters(): List<KphpDocInheritParameterDecl> =
        when (val stub = this.greenStub) {
            null -> getParametersFromAst()
            else -> fromStubs(stub)
        }

    private fun fromStubs(stub: PhpDocTagStub): List<KphpDocInheritParameterDecl> {
        val value = stub.value ?: return emptyList()
        if (value.isEmpty()) {
            return emptyList()
        }

        return value.split(',').map { type ->
            val parts = type.split(':')
            if (parts.size != 3) {
                return@map KphpDocInheritParameterDecl(name, emptyList(), name)
            }

            val (name, typesList, text) = parts
            val types = typesList.split(".")

            KphpDocInheritParameterDecl(name, types, text.replace(';', ','))
        }
    }

    private fun getParametersFromAst(): List<KphpDocInheritParameterDecl> {
        val args = mutableListOf<KphpDocInheritParameterDecl>()
        var child = this.firstChild
        while (child != null) {
            if (child is KphpDocInheritParameterDeclPsiImpl)
                args.add(child.decl())
            child = child.nextSibling
        }
        return args
    }
}
