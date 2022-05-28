package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.resolve.types.PhpCharBasedTypeKey
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.helpers.toStringAsNested

class CallableTypeProvider : PhpTypeProvider4 {
    private val KEY = object : PhpCharBasedTypeKey() {
        override fun getKey(): Char = 'Λ'
    }

    override fun getKey() = KEY.key

    override fun getType(el: PsiElement): PhpType? {
        if (el !is Function) return null
        if (el.name.isNotEmpty()) return null

        val params = el.parameters.joinToString("&&") {
            it.type.toStringAsNested(";;")
        }

        val returnType = el.typeDeclaration?.type?.toStringAsNested(";;") ?: "void"

        return PhpType().add(KEY.sign("$params,$returnType"))
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        val incompleteType = incompleteTypeStr.substring(2)

        val (paramsRawTypeString, returnRawTypeString) = incompleteType.split(",")
        val params = paramsRawTypeString.split("&&")
        val returnRawTypes = returnRawTypeString.split(";;")

        val paramTypes = params.map { param ->
            if (param.isEmpty()) {
                return@map PhpType.MIXED
            }
            val types = param.split(";;")
            val type = PhpType()
            types.forEach { type.add(it).global(project) }
            type
        }
        val paramTypesString = paramTypes.joinToString(",") { it.toString() }

        val returnType = PhpType()
        returnRawTypes.forEach { returnType.add(it).global(project) }
        val returnTypeString = returnType.toString()

        val callableType = "\\Callable($paramTypesString):$returnTypeString"
        return PhpType().add(callableType)
    }

    override fun getBySignature(
        typeStr: String,
        visited: MutableSet<String>?,
        depth: Int,
        project: Project?
    ) = null
}
