package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.resolve.types.PhpCharBasedTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeForcing
import com.vk.kphpstorm.exphptype.ExPhpTypeGenericsT
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericConstructorCall
import kotlin.math.min

class GenericClassesTypeProvider : PhpTypeProvider4 {
    companion object {
        val SEP = "―"
        val KEY = object : PhpCharBasedTypeKey() {
            override fun getKey() = '±'
        }
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement?): PhpType? {
        // new A/*<...args>*/
        if (p is NewExpression) {
            val classRef = p.classReference ?: return null
            val fqn = classRef.fqn + ".__construct"
            val data = IndexingGenericFunctionCall(fqn, p.parameters, p, SEP).pack()
            return PhpType().add(KEY.sign(data))
        }

        return null
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        val packedData = incompleteTypeStr.substring(2)

        val call = ResolvingGenericConstructorCall(project)
        if (!call.unpack(packedData)) {
            return null
        }

        if (!call.klass!!.isGeneric()) {
            return null
        }

        val specialization = call.specialization()

        val specializationNameMap = mutableMapOf<String, ExPhpType>()

        for (i in 0 until min(call.genericTs.size, specialization.size)) {
            specializationNameMap[call.genericTs[i].name] = specialization[i]
        }

        val genericsTypes = call.genericTs.map { ExPhpTypeGenericsT(it.name) }
        val type = ExPhpTypeTplInstantiation(call.klass!!.fqn, genericsTypes)

        val methodTypeSpecialized = type.instantiateGeneric(specializationNameMap)
        return ExPhpTypeForcing(methodTypeSpecialized).toPhpType()
    }

    override fun getBySignature(
        typeStr: String,
        visited: MutableSet<String>?,
        depth: Int,
        project: Project?
    ) = null
}
