package com.vk.kphpstorm.generics

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.vk.kphpstorm.exphptype.ExPhpType

/**
 * Ввиду того, что мы не можем резолвить функции во время вывода типов,
 * нам необходимо выделить всю необходимую информацию для дальнейшего
 * вывода.
 *
 * Таким образом данный класс выделяет типы из явного списка инстанциации
 * и выводит типы аргументов для вызова. Полученные данные пакуются в
 * строку.
 *
 * Полученная строка может быть передана далее в [ResolvingGenericFunctionCall.unpack],
 * для дальнейшей обработки.
 */
class IndexingGenericFunctionCall(
    private val fqn: String,
    private val callArgs: Array<PsiElement>,
    private val reference: PsiElement,
    private val separator: String = "@@",
) {
    private val explicitSpecsPsi = GenericUtil.findInstantiationComment(reference)

    fun pack(): String? {
        val file = reference.containingFile as PhpFile
//        if (fqn.contains(".")) {
//            val (className, functionName) = fqn.split(".")
//            val klass = file.topLevelDefs[className].firstOrNull() as? PhpClass
//            if (klass != null) {
//                val method = klass.findOwnMethodByName(functionName)
//                if (method != null && !method.isGeneric()) {
//                    return null
//                }
//            }
//        }
//        val function = (reference.containingFile as PhpFile).topLevelDefs[fqn].firstOrNull() as? Function
//        if (function != null && !function.isGeneric()) {
//            return null
//        }

        val explicitSpecsString = extractExplicitGenericsT().joinToString("$$")
        val callArgsString = argumentsTypes().joinToString("$$") {
//            if (it.types.size == 2) {
//                val containsUnresolved = it.types.find { type -> type.startsWith("#") } != null
//                val containsResolved = it.types.find { type -> !type.startsWith("#") } != null
//                if (containsUnresolved && containsResolved) {
//                    return@joinToString it.types.find { type -> !type.startsWith("#") }.toString()
//                }
//            }

            // Это необходимо здесь так как например для выражения [new Boo] тип будет #_\int и \Boo
            // и если мы сохраним его как #_\int|\Boo, то в дальнейшем тип будет "#_\int|\Boo", и
            // этот тип не разрешится верно, поэтому сохраняем типы через стрелочку, таким образом
            // внутри PhpType типы будут также разделены, как были на момент сохранения здесь
            if (it.types.size == 1) {
                it.toString()
            } else {
                it.types.joinToString("→")
            }
        }

        return "${fqn}$separator$explicitSpecsString$separator$callArgsString$separator"
    }

    private fun argumentsTypes(): List<PhpType> {
        return callArgs.filterIsInstance<PhpTypedElement>().map { it.type }
    }

    private fun extractExplicitGenericsT(): List<ExPhpType> {
        if (explicitSpecsPsi == null) return emptyList()
        return explicitSpecsPsi.instantiationTypes()
    }
}
