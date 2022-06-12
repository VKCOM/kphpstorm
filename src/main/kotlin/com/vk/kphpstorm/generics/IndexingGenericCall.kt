package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * Type inference in PhpStorm has two steps:
 *
 * - Indexing, collection of types based only on local information from the file, without access to others.
 * Because of this, for complex types that depend on information from other files,
 * we need to pack all the necessary information into a string, what class [IndexingGenericCall] does.
 * PhpStorm calls these types Incomplete, it requires additional processing in the second stage.
 *
 * - Resolving Incomplete types.
 * At this point, PhpStorm converts the resulting Incomplete to Complete types.
 * This is done by the [ResolvingGenericCallBase] class.
 *
 * A typical example of using this class with [ResolvingGenericCallBase] is calling the [pack] method in the
 * [com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4.getType] method.
 *
 * Then calling the [ResolvingGenericCallBase.resolve] method in the method
 * [com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4.complete].
 *
 * Note that [separator] must be unique for different types of calls (e.g. functions, methods, constructors).
 */
class IndexingGenericCall(
    private val fqn: String,
    private val arguments: Array<PsiElement>,
    reference: PsiElement,
    private val separator: String,
) {
    companion object {
        const val START_TYPE = '⋙'
        const val END_TYPE = '⋘'
    }

    private val explicitSpecsPsi = GenericUtil.findInstantiationComment(reference)

    /**
     * Packs all data into a string so that [ResolvingGenericCallBase.resolve] can then unpack
     * this data and infer types when indexing is complete.
     *
     * @return packed call data
     */
    fun pack(): String {
        val explicitSpecsString = extractExplicitGenericsT().joinToString("$$")
        val callArgsString = argumentTypes().joinToString("$$") {
            // Это необходимо здесь так как например для выражения [new Boo] тип будет #_\int и \Boo
            // и если мы сохраним его как #_\int|\Boo, то в дальнейшем тип будет "#_\int|\Boo", и
            // этот тип не разрешится верно, поэтому сохраняем типы через стрелочку, таким образом
            // внутри PhpType типы будут также разделены, как были на момент сохранения здесь

            // TODO: Добавить поддержку Incomplete типов? Чтобы работало даже без тайпхинтов.
            if (it.typesWithParametrisedParts.firstOrNull()?.startsWith("\\Closure<") == true) {
                val rawType = it.typesWithParametrisedParts.first()
                val parts = PhpType.getParametrizedParts(rawType).map { type -> type.replace("ᤓ", "/") }
                    .map {type ->
                        if (type.startsWith("\\int<") && type.endsWith("int")) {
                            "int"
                        } else {
                            type
                        }
                    }

                val returnType = parts.last()
                val paramTypes = parts.dropLast(1).map { type -> type.ifEmpty { "mixed" } }

                return@joinToString "callable(${paramTypes.joinToString(",")}):$returnType"
            }

            if (it.types.size == 1) {
                it.toString()
            } else {
                it.types.joinToString("→")
            }
        }

        return "$START_TYPE$fqn$separator$explicitSpecsString$separator$callArgsString$END_TYPE"
    }

    private fun argumentTypes() = arguments.filterIsInstance<PhpTypedElement>().map { it.type }
    private fun extractExplicitGenericsT() = explicitSpecsPsi?.instantiationTypes() ?: emptyList()
}
