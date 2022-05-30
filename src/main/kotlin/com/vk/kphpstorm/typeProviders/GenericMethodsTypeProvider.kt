package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.resolve.types.PhpCharTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpTypeGenericsT
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericMethodCall
import com.vk.kphpstorm.helpers.toExPhpType

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
            val paramTag = parentFunction.docComment?.getParamTagByName(p.name) ?: return null
            val docType = paramTag.type.toExPhpType() ?: return null
            if (docType is ExPhpTypeGenericsT) {
                val decl = parentFunction.genericNames().find { it.name == docType.nameT } ?: return null
                val type = decl.extendsType ?: decl.defaultType ?: return null

                return PhpType().add(type.toPhpType()).add(docType.toPhpType())
            }
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
                        !type.startsWith("#")
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

    override fun complete(incompleteType: String, project: Project) =
        ResolvingGenericMethodCall(project).resolve(incompleteType)

    override fun getBySignature(t: String, v: MutableSet<String>, d: Int, p: Project) = null
}
