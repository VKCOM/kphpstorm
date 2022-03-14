package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * 'T' — is genericsT when it's defined in @kphp-generic, then it's genericsT on resolve, not class T
 * @see com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl.getType
 */
class ExPhpTypeGenericsT(val nameT: String) : ExPhpType {
    override fun toString() = "%$nameT"

    override fun toHumanReadable(expr: PhpPsiElement) = "%$nameT"

    override fun toPhpType(): PhpType {
        return PhpType().add("%$nameT")
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return null
    }

    override fun instantiateGeneric(nameMap: Map<String, ExPhpType>): ExPhpType {
        return nameMap[nameT] ?: this
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny -> true
        // todo shall we add any strict rules here?
        else            -> true
    }
}
