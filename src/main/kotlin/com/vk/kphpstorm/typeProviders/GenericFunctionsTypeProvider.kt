package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.resolve.types.PhpCharBasedTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeForcing
import com.vk.kphpstorm.generics.GenericUtil.isReturnGeneric
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericFunctionCall
import com.vk.kphpstorm.helpers.toExPhpType
import kotlin.math.min

class GenericFunctionsTypeProvider : PhpTypeProvider4 {
    companion object {
        val KEY = object : PhpCharBasedTypeKey() {
            override fun getKey() = 'П'
        }
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement): PhpType? {
        if (p !is FunctionReference || p is MethodReference) {
            return null
        }

        val data = IndexingGenericFunctionCall(p.fqn!!, p.parameters, p).pack()
        return PhpType().add(KEY.sign(data))
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        val packedData = incompleteTypeStr.substring(2)

        val call = ResolvingGenericFunctionCall(project)
        if (!call.unpack(packedData)) {
            return null
        }

        // Если возвращаемый тип функции не зависит от шаблонного типа,
        // то нет смысла как-то уточнять ее тип.
        if (!call.function.isReturnGeneric()) {
            return null
        }

        val specialization = call.specialization()

        val specializationNameMap = mutableMapOf<String, ExPhpType>()

        for (i in 0 until min( call.genericTs.size, specialization.size)) {
            specializationNameMap[call.genericTs[i].name] = specialization[i]
        }

        val methodReturnTag = call.function.docComment?.returnTag ?: return null
        val methodTypeParsed = methodReturnTag.type.toExPhpType() ?: return null
        val methodTypeSpecialized = methodTypeParsed.instantiateGeneric(specializationNameMap)

        return ExPhpTypeForcing(methodTypeSpecialized).toPhpType().add(methodTypeSpecialized.toPhpType())
    }

    override fun getBySignature(
        typeStr: String,
        visited: MutableSet<String>?,
        depth: Int,
        project: Project?
    ) = null
}
