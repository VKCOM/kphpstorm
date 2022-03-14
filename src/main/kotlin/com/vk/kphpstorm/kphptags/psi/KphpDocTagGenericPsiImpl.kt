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
    fun getGenericArguments(): List<String> =
            when (val stub = this.greenStub) {
                null -> getGenericArgumentsFromAst()
                else -> stub.value.let {    // stub value is 'T1,T2'
                    if (it != null && it.isNotEmpty()) it.split(',')
                    else listOf()
                }
            }

    private fun getGenericArgumentsFromAst(): List<String> {
        val args = mutableListOf<String>()
        var child = this.firstChild
        while (child != null) {
            if (child is KphpDocGenericParameterDeclPsiImpl)
                args.add(child.name)
            child = child.nextSibling
        }
        return args
    }
}
