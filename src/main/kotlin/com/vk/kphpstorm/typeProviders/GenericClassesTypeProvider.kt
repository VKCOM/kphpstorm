package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.NewExpressionImpl
import com.jetbrains.php.lang.psi.resolve.types.PhpCharBasedTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeGenericsT
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.generics.GenericUtil.isReturnGeneric
import com.vk.kphpstorm.generics.IndexingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericConstructorCall
import com.vk.kphpstorm.generics.ResolvingGenericFunctionCall
import com.vk.kphpstorm.generics.ResolvingGenericMethodCall
import com.vk.kphpstorm.helpers.toExPhpType
import kotlin.math.min

class GenericClassesTypeProvider : PhpTypeProvider4 {
    companion object {
        private val KEY = object : PhpCharBasedTypeKey() {
            override fun getKey(): Char {
                return '±'
            }
        }
    }

    override fun getKey() = KEY.key

    override fun getType(p: PsiElement?): PhpType? {
        // $v->f()
        if (p is MethodReferenceImpl && !p.isStatic) {
            val methodName = p.name ?: return null
            val lhs = p.classReference ?: return null
            val lhsType = lhs.type

            val resultType = PhpType()
            lhsType.types.forEach { type ->
                val fqn = "$type.$methodName"
                val data = IndexingGenericFunctionCall(fqn, p.parameters, p, "@MC@").pack() ?: return@forEach

                resultType.add("#±:$data")
            }

            return resultType
        }

        // new A/*<...args>*/
        if (p is NewExpressionImpl) {
            val classRef = p.classReference ?: return null
            val fqn = classRef.fqn + "__construct"
            val data = IndexingGenericFunctionCall(fqn, p.parameters, p, "@CO@").pack() ?: return null
            return PhpType().add("#±$data")
        }

        return null
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        if (!KEY.signed(incompleteTypeStr)) {
            return null
        }

        val packedData = incompleteTypeStr.substring(2)

        if (packedData.startsWith(":")) {
            return completeMethodCall(project, packedData)
        }

        if (packedData.contains("__construct")) {
            return completeConstruct(project, packedData)
        }

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
            specializationNameMap[ call.genericTs[i]] = specialization[i]
        }

        val methodReturnTag = call.function.docComment?.returnTag ?: return null
        val methodTypeParsed = methodReturnTag.type.toExPhpType() ?: return null
        val methodTypeSpecialized = methodTypeParsed.instantiateGeneric(specializationNameMap)

        return methodTypeSpecialized.toPhpType()
    }

    private fun completeConstruct(project: Project, packedData: String): PhpType? {
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
            specializationNameMap[call.genericTs[i]] = specialization[i]
        }

        val genericsTypes = call.genericTs.map { ExPhpTypeGenericsT(it) }
        val type = ExPhpTypeTplInstantiation(call.klass!!.fqn, genericsTypes)

        val methodTypeSpecialized = type.instantiateGeneric(specializationNameMap)
        return methodTypeSpecialized.toPhpType()
    }

    private fun completeMethodCall(project: Project, packedData: String): PhpType? {
        val call = ResolvingGenericMethodCall(project)
        if (!call.unpack(packedData)) {
            return null
        }

        val specialization = call.specialization()

        val specializationNameMap = mutableMapOf<String, ExPhpType>()

        for (i in 0 until min(call.genericTs.size, specialization.size)) {
            specializationNameMap[call.genericTs[i]] = specialization[i]
        }

        if (call.classGenericType != null) {
            for (i in 0 until min(call.classGenericTs.size, call.classGenericType!!.specializationList.size)) {
                specializationNameMap[call.classGenericTs[i]] = call.classGenericType!!.specializationList[i]
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