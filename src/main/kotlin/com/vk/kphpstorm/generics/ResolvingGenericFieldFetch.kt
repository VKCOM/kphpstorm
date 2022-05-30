package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
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

        return specializedType.toPhpType()
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
        val className = fqn.substring(0, dotIndex)
        val methodName = fqn.substring(dotIndex + 1)

        val classType = PhpType().add(className).global(project)
        val parsed = classType.toExPhpType()
        val instantiation = parsed?.getInstantiation()
            ?:
            // Если не удалось найти класс, значит вывести
            // тип поля мы не сможем, поэтому заканчиваем распаковку.
            return false

        if (klass == null) {
            klass = PhpIndex.getInstance(project).getClassesByFQN(instantiation.classFqn).firstOrNull() ?: return false
        }

        field = klass?.findFieldByName(methodName, false) ?: return false

        // Так как это поле статическое, мы не сможем вывести для него тип.
        if (!field!!.modifier.isStatic) {
            return true
        }

        argumentsTypes = emptyList()
        explicitGenericsT = emptyList()

        return true
    }
}
