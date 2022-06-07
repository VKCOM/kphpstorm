package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeForcing
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.getInstantiations
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.typeProviders.GenericFieldsTypeProvider

class ResolvingGenericFieldFetch(project: Project) : ResolvingGenericBase(project) {
    private var field: Field? = null

    override var klass: PhpClass? = null
    override var classGenericType: ExPhpTypeTplInstantiation? = null
    override var classGenericTs: List<KphpDocGenericParameterDecl> = emptyList()

    // No parameters.
    override var parameters: Array<Parameter> = emptyArray()

    // No own generics.
    override var genericTs: List<KphpDocGenericParameterDecl> = emptyList()

    override fun instantiate(): PhpType? {
        val specializationNameMap = specialization()

        val varTag = field?.docComment?.varTag ?: return null
        val exType = varTag.type.toExPhpType() ?: return null
        val specializedType = exType.instantiateGeneric(specializationNameMap)

        return PhpType().add(specializedType.toPhpType()).add(ExPhpTypeForcing(specializedType).toPhpType())
    }

    /**
     * См. комментарий в [ResolvingGenericFunctionCall.unpackImpl]
     */
    override fun unpackImpl(packedData: String): Boolean {
        // If className is resolved
        // \SomeName.fieldName...
        if (beginCompleted(packedData)) {
            val firstSeparator = packedData.indexOf(GenericFieldsTypeProvider.SEP)
            val fqn = packedData.substring(1, firstSeparator)
            val className = fqn.split('.').first()
            // Если имя класса не содержит скобок, значит вывести
            // тип поля мы не сможем, поэтому заканчиваем распаковку.
            if (!className.contains("(")) {
                return false
            }
        }

        val data = resolveSubTypes(packedData)
        val parts = safeSplit(data, 3, GenericFieldsTypeProvider.SEP) ?: return false

        val fqn = parts[0]

        val dotIndex = fqn.lastIndexOf('.')
        val classRawName = fqn.substring(0, dotIndex)
        val methodName = fqn.substring(dotIndex + 1)

        val classType = PhpType().add(classRawName).global(project)
        val parsed = classType.toExPhpType()

        val instantiations = parsed?.getInstantiations()
        val foundInstantiation = instantiations?.firstOrNull {
            val klass = PhpIndex.getInstance(project).getClassesByFQN(it.classFqn).firstOrNull()
            val field = klass?.findFieldByName(methodName, false)

            field != null
        } ?: return false

        classGenericType = foundInstantiation

        klass = PhpIndex.getInstance(project).getClassesByFQN(foundInstantiation.classFqn).firstOrNull() ?: return false
        field = klass?.findFieldByName(methodName, false) ?: return false

        classGenericTs = klass!!.genericNames()

        argumentsTypes = emptyList()
        explicitGenericsT = emptyList()

        return true
    }
}
