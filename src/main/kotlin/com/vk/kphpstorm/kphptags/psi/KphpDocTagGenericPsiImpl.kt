package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub

/**
 * Implementation of '@kphp-generic' tag — created either from stub or from ast
 * @see KphpDocElementTypes.kphpDocTagGeneric
 */
class KphpDocTagGenericPsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
    constructor(stub: PhpDocTagStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    // important! this function can be called when current file is not loaded,
    // but we store all necessary information in stub
    fun getGenericArguments(): List<String> = getGenericArgumentsWithExtends().map { it.name }

    fun getGenericArgumentsWithExtends(): List<KphpDocGenericParameterDecl> =
            when (val stub = this.greenStub) {
                null -> getGenericArgumentsFromAst()
                else -> stub.value.let {    // stub value is 'T1,T2:ExtendsClass,T3=default'
                    if (it != null && it.isNotEmpty()) {
                        it.split(',').mapNotNull { type ->
                            val colonIndex = type.indexOf(':')
                            val parts = type.split(':', '=')
                            if (parts.isEmpty()) return@mapNotNull null
                            val name = parts[0]

                            if (parts.size == 1) {
                                KphpDocGenericParameterDecl(name, null, null)
                            } else if (parts.size == 2) {
                                if (colonIndex != -1) {
                                    KphpDocGenericParameterDecl(name, parts[1], null)
                                } else {
                                    KphpDocGenericParameterDecl(name, null, parts[1])
                                }
                            } else if (parts.size == 3) {
                                KphpDocGenericParameterDecl(name, parts[1], parts[2])
                            } else {
                                null
                            }
                        }
                    }
                    else listOf()
                }
            }

    private fun getGenericArgumentsFromAst(): List<KphpDocGenericParameterDecl> {
        val args = mutableListOf<KphpDocGenericParameterDecl>()
        var child = this.firstChild
        while (child != null) {
            if (child is KphpDocGenericParameterDeclPsiImpl)
                args.add(child.decl())
            child = child.nextSibling
        }
        return args
    }
}
