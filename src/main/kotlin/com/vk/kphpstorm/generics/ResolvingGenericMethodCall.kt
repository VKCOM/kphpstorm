package com.vk.kphpstorm.generics

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.Parameter
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeGenericsT
import com.vk.kphpstorm.exphptype.ExPhpTypeTplInstantiation
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.getInstantiation
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocGenericParameterDecl
import com.vk.kphpstorm.typeProviders.GenericMethodsTypeProvider

class ResolvingGenericMethodCall(project: Project) : ResolvingGenericBase(project) {
    var klass: PhpClass? = null
    var method: Method? = null
    var classGenericType: ExPhpTypeTplInstantiation? = null
    override lateinit var parameters: Array<Parameter>
    override lateinit var genericTs: List<KphpDocGenericParameterDecl>
    lateinit var classGenericTs: List<KphpDocGenericParameterDecl>

    override fun klass(): PhpClass? = klass

    private fun unpackRecursive(data: String): Boolean {
        val sep = GenericMethodsTypeProvider.SEP

        if (GenericMethodsTypeProvider.KEY.signed(data)) {
            val parts = getAtLeast(data.substring(2), 3, sep) ?: return false

            val joinedType = (parts.take(3).joinToString(sep))
                .removePrefix("#" + GenericMethodsTypeProvider.KEY)
                .removePrefix("#" + GenericMethodsTypeProvider.KEY)
            val count = joinedType.count { it == sep[0] } / 2
            var forResolve = joinedType
            for (i in 0 until count) {
                forResolve = GenericMethodsTypeProvider.KEY.sign(forResolve)
            }

            val fqnResolved =
                PhpType().add(forResolve).global(project).toExPhpType()
            if (fqnResolved == null) {
                return false
            }

            val remainingPart = parts.getOrElse(3) { "" }.removePrefix(sep)
            val newPackedData = "$fqnResolved$remainingPart"

            return unpackRecursive(newPackedData)
        } else {
            // Не обрабатываем строки, которые содержат типы от PhpStorm
            if (data.contains("#M") || data.contains("#C")) {
                return false
            }
            val parts = getAtLeast(data, 3, sep)
            if (parts == null) {
                return false
            }

            val fqn = parts[0]

            val dotIndex = fqn.lastIndexOf('.')
            val classRawName = fqn.substring(0, dotIndex)
            val methodName = fqn.substring(dotIndex + 1)

            val classType = PhpType().add(classRawName).global(project)
            val parsed = classType.toExPhpType()
            val instantiation = parsed?.getInstantiation()

            val className = if (instantiation != null && instantiation.specializationList.first() !is ExPhpTypeGenericsT) {
                classGenericType = instantiation
                instantiation.classFqn
            } else {
                classRawName
            }

            klass = PhpIndex.getInstance(project).getClassesByFQN(className).firstOrNull() ?: return false
            method = klass!!.findMethodByName(methodName)
            if (method == null) {
                return false
            }

            parameters = method!!.parameters
            genericTs = method!!.genericNames()

            // Не устанавливаем параметры класса, так как это статический вызов
            if (!method!!.isStatic) {
                classGenericTs = klass!!.genericNames()
            }

            explicitGenericsT = unpackTypeArray(parts[1])
            argumentsTypes = unpackTypeArray(parts[2])

            return true
        }
    }

    override fun unpackImpl(packedData: String): Boolean {
        return unpackRecursive(packedData.removePrefix(":"))
    }
}
