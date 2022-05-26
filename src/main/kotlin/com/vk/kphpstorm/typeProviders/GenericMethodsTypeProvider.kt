package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.resolve.types.PhpCharBasedTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericMethodCall
import com.vk.kphpstorm.helpers.toExPhpType
import kotlin.math.min

class GenericMethodsTypeProvider : PhpTypeProvider4 {
    companion object {
        val SEP = "⁓"
        val KEY = object : PhpCharBasedTypeKey() {
            override fun getKey(): Char {
                return 'ω'
            }
        }
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement?): PhpType? {
        // $v->f()
        if (p is MethodReference && !p.isStatic) {
            val methodName = p.name ?: return null
            val lhs = p.classReference ?: return null
            val lhsTypes = lhs.type.types.filter { type ->
                GenericClassesTypeProvider.KEY.signed(type) || KEY.signed(type) || !type.startsWith("#")
            }

            val resultType = PhpType()
            lhsTypes.forEach { type ->
                val fqn = "$type.$methodName"
                val data = IndexingGenericFunctionCall(fqn, p.parameters, p, SEP).pack() ?: return@forEach

                resultType.add(KEY.sign(data))
            }

            return resultType
        }

        return null
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        if (!KEY.signed(incompleteTypeStr)) {
            return null
        }

        val packedData = incompleteTypeStr.substring(2)

        val call = ResolvingGenericMethodCall(project)
        if (!call.unpack(packedData)) {
            return null
        }

        val specialization = call.specialization()

        val specializationNameMap = mutableMapOf<String, ExPhpType>()

        for (i in 0 until min(call.genericTs.size, specialization.size)) {
            specializationNameMap[call.genericTs[i].name] = specialization[i]
        }

        if (call.classGenericType != null) {
            for (i in 0 until min(call.classGenericTs.size, call.classGenericType!!.specializationList.size)) {
                specializationNameMap[call.classGenericTs[i].name] = call.classGenericType!!.specializationList[i]
            }
        }

        val methodReturnTag = call.method?.docComment?.returnTag ?: return null
        val methodTypeParsed = methodReturnTag.type.toExPhpType() ?: return null
        val methodTypeSpecialized = methodTypeParsed.instantiateGeneric(specializationNameMap)

        return methodTypeSpecialized.toPhpType()
    }


    override fun getBySignature(
        typeStr: String,
        visited: MutableSet<String>?,
        depth: Int,
        project: Project?
    ): MutableCollection<PhpNamedElement>? {
        return null
    }
}
