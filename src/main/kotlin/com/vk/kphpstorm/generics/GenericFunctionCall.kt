package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.generics.GenericUtil.findInstantiationComment
import com.vk.kphpstorm.generics.GenericUtil.genericNames
import com.vk.kphpstorm.generics.GenericUtil.isGeneric
import com.vk.kphpstorm.helpers.toExPhpType


class GenericFunctionCall(private val call: FunctionReference) : GenericCall(call.project) {
    override val callArgs: Array<PsiElement> = call.parameters
    override val argumentsTypes: List<ExPhpType?> = callArgs
        .filterIsInstance<PhpTypedElement>().map { it.type.global(project).toExPhpType() }
    override val explicitSpecsPsi = findInstantiationComment(call)

    private val function: Function? = call.resolve() as? Function

    init {
        init()
    }

    override fun element() = call

    override fun function() = function

    override fun isResolved() = function != null

    override fun genericNames() = function?.genericNames() ?: emptyList()

    override fun ownGenericNames() = genericNames()

    override fun isGeneric() = function()?.isGeneric() == true

    override fun toString(): String {
        val function = function()
        val explicit = explicitSpecs.joinToString(",")
        val implicit = implicitSpecs.joinToString(",")
        return "${function?.fqn ?: "UnknownFunction"}<$explicit>($implicit)"
    }
}

