package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.vk.kphpstorm.exphptype.ExPhpTypeInstance
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.exphptype.PhpTypeToExPhpTypeParsing
import com.vk.kphpstorm.helpers.toExPhpType

data class KphpDocGenericParameterDecl(
    val name: String,
    private val extendsTypeString: String?,
    private val defaultTypeString: String?,
) {
    val extendsType = extendsTypeString?.let { PhpTypeToExPhpTypeParsing.parseFromString(it) }
    val defaultType = defaultTypeString?.let { PhpTypeToExPhpTypeParsing.parseFromString(it) }

    fun toHumanReadable(context: PsiElement): String {
        val type = StringBuilder()
        type.append(name)

        if (extendsTypeString != null) {
            type.append(": ")

            when (extendsType) {
                is ExPhpTypeInstance -> {
                    type.append(instanceToString(context, extendsType))
                }
                is ExPhpTypePipe -> {
                    val pipe = extendsType.items.joinToString(" | ") {
                        if (it is ExPhpTypeInstance) {
                            instanceToString(context, it)
                        } else {
                            it.toString()
                        }
                    }
                    type.append(pipe)
                }
                else -> {
                    type.append(extendsTypeString.removePrefix("\\"))
                }
            }
        }

        return type.toString()
    }

    private fun instanceToString(
        context: PsiElement,
        extendsType: ExPhpTypeInstance
    ): String {
        val part =
            PhpCodeInsightUtil.findScopeForUseOperator(context)?.let {
                PhpCodeInsightUtil.createQualifiedName(
                    it,
                    extendsType.fqn
                )
            }
        return part ?: extendsType.fqn
    }
}

/**
 * Inside '@kphp-generic T1, T2: ExtendsClass, T3 = default' — 'T1', 'T2: ExtendsClass' and 'T3 = default'
 * are separate psi elements of this impl.
 *
 * @see KphpDocTagGenericElementType.getTagParser
 */
class KphpDocGenericParameterDeclPsiImpl(node: ASTNode) : PhpDocPsiElementImpl(node), PhpDocRef {
    companion object {
        val elementType = PhpDocElementType("phpdocGenericParameterDecl")
    }

    val namePsi: PsiElement? = firstChild
    var extendsTypePsi: PhpDocTypeImpl? = null
    var defaultTypePsi: PhpDocTypeImpl? = null

    init {
        val types = findChildrenByClass(PhpDocTypeImpl::class.java)

        types.forEach {
            val textBefore = PsiTreeUtil.findSiblingBackward(it, PhpDocTokenTypes.DOC_TEXT, null)
            if (textBefore?.text == ":") {
                extendsTypePsi = it
            } else if (textBefore?.text == "=") {
                defaultTypePsi = it
            }
        }
    }

    override fun getName() = namePsi?.text ?: ""

    fun decl(): KphpDocGenericParameterDecl {
        return KphpDocGenericParameterDecl(
            name,
            extendsTypePsi?.type?.toExPhpType()?.toString(),
            defaultTypePsi?.type?.toExPhpType()?.toString()
        )
    }
}
