package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpCharTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericMethodCall
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl

class GenericMethodsTypeProvider : PhpTypeProvider4 {
    companion object {
        const val SEP = "⁓"
        val KEY = PhpCharTypeKey('ω')
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement?): PhpType? {
        if (p is Parameter) {
            // Для параметров если они шаблонные и имеют дефолтный тип или тип extends, то
            // возвращаем здесь этот тип тем самым типизируя частично код внутри функции.
            val parentFunction = p.parentOfType<Function>() ?: return null
            val parentClass = p.parentOfType<PhpClass>()
            val paramTag = parentFunction.docComment?.getParamTagByName(p.name) ?: return null
            val docType = paramTag.type.toExPhpType() ?: return null
            val genericNames = parentFunction.genericNames() + (parentClass?.genericNames() ?: emptyList())

            return instantiateDocType(docType, genericNames)
        }

        if (p is Field) {
            val parentClass = p.containingClass ?: return null
            val paramTag = p.docComment?.varTag ?: return null
            val docType = paramTag.type.toExPhpType() ?: return null
            val genericNames = parentClass.genericNames()

            return instantiateDocType(docType, genericNames)
        }

        // $v->f() or ClassName::f()
        if (p is MethodReference) {
            val methodName = p.name ?: return null
            val lhs = p.classReference ?: return null
            val lhsTypes = lhs.type.types.filter { type ->
                GenericClassesTypeProvider.KEY.signed(type) ||
                        GenericFunctionsTypeProvider.KEY.signed(type) ||
                        GenericFieldsTypeProvider.KEY.signed(type) ||
                        KEY.signed(type) ||
                        (!type.startsWith("#") && !type.startsWith("%"))
            }

            val resultType = PhpType()
            lhsTypes.forEach { type ->
                val fqn = "$type.$methodName"
                val data = IndexingGenericFunctionCall(fqn, p.parameters, p, SEP).pack()

                resultType.add(KEY.sign(data))
            }

            return resultType
        }

        return null
    }

    private fun instantiateDocType(docType: ExPhpType, genericNames: List<KphpDocGenericParameterDecl>): PhpType? {
        if (!docType.isGeneric()) {
            return null
        }

        val instantiationMap = genericNames.mapNotNull {
            it.name to (it.extendsType ?: it.defaultType ?: return@mapNotNull null)
        }.toMap()

        if (instantiationMap.isEmpty()) {
            return null
        }

        val type = docType.instantiateGeneric(instantiationMap)
        return PhpType().add(type.toPhpType()).add(docType.toPhpType())
    }

    override fun complete(incompleteType: String, project: Project) =
        ResolvingGenericMethodCall(project).resolve(incompleteType)

    override fun getBySignature(t: String, v: MutableSet<String>, d: Int, p: Project) = null
}
