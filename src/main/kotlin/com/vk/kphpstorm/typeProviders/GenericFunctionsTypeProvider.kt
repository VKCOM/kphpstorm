package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.resolve.types.PhpCharTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericFunctionCall

class GenericFunctionsTypeProvider : PhpTypeProvider4 {
    companion object {
        const val SEP = "∃"
        val KEY = PhpCharTypeKey('П')
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement): PhpType? {
        if (p !is FunctionReference || p is MethodReference) {
            return null
        }

        val data = IndexingGenericFunctionCall(p.fqn!!, p.parameters, p, SEP).pack()
        return PhpType().add(KEY.sign(data))
    }

    override fun complete(incompleteType: String, project: Project) =
        ResolvingGenericFunctionCall(project).resolve(incompleteType)

    override fun getBySignature(t: String, v: MutableSet<String>, d: Int, p: Project) = null
}
