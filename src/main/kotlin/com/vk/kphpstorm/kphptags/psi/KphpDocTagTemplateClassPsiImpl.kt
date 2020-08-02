package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.stubs.PhpDocTagStub

/**
 * Implemetation of '@kphp-template-class' tag — created either from stub or from ast
 * @see KphpDocElementTypes.kphpDocTagTemplateClass
 */
class KphpDocTagTemplateClassPsiImpl : PhpDocTagImpl, KphpDocTagImpl {
    constructor(node: ASTNode) : super(node)
    constructor(stub: PhpDocTagStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    // important! this function can be called when current file is not loaded,
    // but we store all necessary information in stub
    fun getTemplateArguments(): List<String> =
            when (val stub = this.greenStub) {
                null -> getTemplateArgumentsFromAst()
                else -> stub.value.let {    // stub value is 'T1,T2'
                    if (it != null && it.isNotEmpty()) it.split(',')
                    else listOf()
                }
            }

    private fun getTemplateArgumentsFromAst(): List<String> {
        val args = mutableListOf<String>()
        var child = this.firstChild
        while (child != null) {
            if (child is KphpDocTplParameterDeclPsiImpl)
                args.add(child.text)
            child = child.nextSibling
        }
        return args
    }
}
