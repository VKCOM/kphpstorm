package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.FieldReference
import com.jetbrains.php.lang.psi.resolve.types.PhpCharTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericFieldFetch

class GenericFieldsTypeProvider : PhpTypeProvider4 {
    companion object {
        const val SEP = "≠"
        val KEY = PhpCharTypeKey('μ')
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement?): PhpType? {
        // $v->a
        if (p is FieldReference && !p.isStatic) {
            val fieldName = p.name ?: return null
            val lhs = p.classReference ?: return null
            val lhsTypes = lhs.type.types.filter { type ->
                GenericClassesTypeProvider.KEY.signed(type) ||
                        GenericFunctionsTypeProvider.KEY.signed(type) ||
                        GenericMethodsTypeProvider.KEY.signed(type) ||
                        KEY.signed(type) ||
                        !type.startsWith("#")
            }

            val resultType = PhpType()
            lhsTypes.forEach { type ->
                val fqn = "$type.$fieldName"
                val data = IndexingGenericFunctionCall(fqn, emptyArray(), p, SEP).pack()

                resultType.add(KEY.sign(data))
            }

            return resultType
        }

        return null
    }

    override fun complete(incompleteType: String, project: Project) =
        ResolvingGenericFieldFetch(project).resolve(incompleteType)

    override fun getBySignature(t: String, v: MutableSet<String>, d: Int, p: Project) = null
}
