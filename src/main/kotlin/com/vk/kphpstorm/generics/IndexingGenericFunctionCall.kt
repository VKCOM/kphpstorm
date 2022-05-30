package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.PhpTypedElement

/**
 * Ввиду того, что мы не можем резолвить функции во время вывода типов,
 * нам необходимо выделить всю необходимую информацию для дальнейшего
 * вывода.
 *
 * Таким образом данный класс выделяет типы из явного списка инстанциации
 * и выводит типы аргументов для вызова. Полученные данные пакуются в
 * строку.
 *
 * Полученная строка может быть передана далее в [ResolvingGenericBase.resolve],
 * для дальнейшей обработки.
 */
class IndexingGenericFunctionCall(
    private val fqn: String,
    private val callArgs: Array<PsiElement>,
    reference: PsiElement,
    private val separator: String,
) {
    companion object {
        const val START_TYPE = '⋙'
        const val END_TYPE = '⋘'
    }

    private val explicitSpecsPsi = GenericUtil.findInstantiationComment(reference)

    fun pack(): String {
        val explicitSpecsString = extractExplicitGenericsT().joinToString("$$")
        val callArgsString = argumentsTypes().joinToString("$$") {
            // Это необходимо здесь так как например для выражения [new Boo] тип будет #_\int и \Boo
            // и если мы сохраним его как #_\int|\Boo, то в дальнейшем тип будет "#_\int|\Boo", и
            // этот тип не разрешится верно, поэтому сохраняем типы через стрелочку, таким образом
            // внутри PhpType типы будут также разделены, как были на момент сохранения здесь

            // TODO: After 2022.2 should be typesWithParametrisedParts
            if (it.types.firstOrNull()?.startsWith("\\Closure<") == true) {
                val rawType = it.types.first().replace("ᤓ", "/")
                val paramsPart = rawType.substring(rawType.indexOf('<') + 1, rawType.lastIndexOf('>'))
                val paramsWithReturnType = paramsPart.split(',')

                val returnType = paramsWithReturnType.last()
                val paramTypes = paramsWithReturnType.dropLast(1).map { type -> type.ifEmpty { "mixed" } }

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

    private fun argumentsTypes() = callArgs.filterIsInstance<PhpTypedElement>().map { it.type }
    private fun extractExplicitGenericsT() = explicitSpecsPsi?.instantiationTypes() ?: emptyList()
}
