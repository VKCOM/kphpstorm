package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.resolve.types.PhpCharBasedTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericConstructorCall

class GenericClassesTypeProvider : PhpTypeProvider4 {
    companion object {
        const val SEP = "―"
        val KEY = object : PhpCharBasedTypeKey() {
            override fun getKey() = '±'
        }
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement?): PhpType? {
        // new A/*<...args>*/()
        if (p is NewExpression) {
            val classRef = p.classReference ?: return null
            val fqn = classRef.fqn + ".__construct"
            val data = IndexingGenericFunctionCall(fqn, p.parameters, p, SEP).pack()
            return PhpType().add(KEY.sign(data))
        }

        return null
    }

    override fun complete(incompleteType: String, project: Project) =
        ResolvingGenericConstructorCall(project).resolve(incompleteType)

    override fun getBySignature(t: String, v: MutableSet<String>, d: Int, p: Project) = null
}
