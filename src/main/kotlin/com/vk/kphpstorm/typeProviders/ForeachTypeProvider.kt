package com.vk.kphpstorm.typeProviders

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.ForeachStatement
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.elements.Variable
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4
import com.vk.kphpstorm.helpers.toExPhpType

/**
 * Purpose:
 * foreach($something as $a) — better infer type of $a with KPHP specifics:
 * 1) foreach on mixed is mixed
 * 2) foreach on (partially) untyped array should lead to any
 *
 * Here we handle only foreach(... as [$k=>] $a), don't handle complex cases like list() instead of $a.
 */
class ForeachTypeProvider : PhpTypeProvider4 {
    override fun getKey(): Char {
        return 'Ф'
    }

    override fun getType(p: PsiElement): PhpType? {
        val foreach =
                if (p is Variable && p.parent is ForeachStatement) p.parent as ForeachStatement
                else return null

        if (foreach.value == p) {
            val arrType = (foreach.array as? PhpTypedElement)?.type ?: return null

            return when {
                arrType.isComplete -> inferTypeOfForeachArgument(arrType)
                else               -> PhpType().apply {
                    arrType.types.forEach {
                        add("#Ф$it")
                    }
                }
            }
        }

        return null
    }

    override fun complete(incompleteTypeStr: String, project: Project): PhpType? {
        if (!incompleteTypeStr.startsWith("#Ф")) {
           return null
        }

        if (incompleteTypeStr.contains("%Ф")) {
            return null
        }

        val arrTypeStr = incompleteTypeStr.substring(2)
        val arrType = PhpType().add(arrTypeStr).global(project)

        return inferTypeOfForeachArgument(arrType)
    }

    override fun getBySignature(typeStr: String, visited: MutableSet<String>?, depth: Int, project: Project?): MutableCollection<PhpNamedElement>? {
        return null
    }

    /**
     * Having ($arr as $argument), infer type of $argument
     */
    private fun inferTypeOfForeachArgument(arrType: PhpType): PhpType? {
        return arrType.toExPhpType()?.getSubkeyByIndex("")?.toPhpType()
    }
}
