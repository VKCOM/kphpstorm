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

    /**
     * Функция возвращающая только имена шаблонных аргументов, без extends и default типов.
     *
     * Эта функция необходима, чтобы в [KphpDocGenericParameterDeclPsiImpl.decl] мы могли
     * вывести типы. Если использовать [getGenericArgumentsWithExtends] вместо, то
     * будет рекурсия, так как для вывода типа класса, нам нужно знать не шаблонный ли это
     * аргумент, а для этого используется [getGenericArgumentsWithExtends] которая вызывает
     * в себе [KphpDocGenericParameterDeclPsiImpl.decl].которая в себе пытается вывести типы.
     */
    fun getOnlyNameGenericArguments(): List<String> =
        when (val stub = this.greenStub) {
            null -> getOnlyNameGenericArgumentsFromAst()
            else -> fromStubs(stub).map { it.name }
        }

    fun getGenericArgumentsWithExtends(): List<KphpDocGenericParameterDecl> =
        when (val stub = this.greenStub) {
            null -> getGenericArgumentsFromAst()
            else -> fromStubs(stub)
        }

    private fun fromStubs(stub: PhpDocTagStub): List<KphpDocGenericParameterDecl> {
        val value = stub.value ?: return emptyList()
        if (value.isEmpty()) {
            return emptyList()
        }

        return value.split(',').mapNotNull { type ->
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

    private fun getOnlyNameGenericArgumentsFromAst(): List<String> {
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
