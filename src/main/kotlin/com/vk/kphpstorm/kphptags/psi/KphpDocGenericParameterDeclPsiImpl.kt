package com.vk.kphpstorm.kphptags.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocElementType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocPsiElementImpl
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypeInstancePsiImpl
import com.vk.kphpstorm.exphptype.psi.ExPhpTypePrimitivePsiImpl

data class KphpDocGenericParameterDecl(
    val name: String,
    val extendsClass: String? = null,
    val defaultType: String? = null
)

fun KphpDocGenericParameterDecl.toHumanReadable(): String {
    val type = StringBuilder()
    type.append(name)

    if (extendsClass != null) {
        type.append(": ")
        type.append(extendsClass.removePrefix("\\"))
    }

    if (defaultType != null) {
        type.append(" = ")
        type.append(defaultType.removePrefix("\\"))
    }

    return type.toString()
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

    private var extendsClass: ExPhpTypeInstancePsiImpl? = null

    /**
     * Can be [ExPhpTypeInstancePsiImpl] or [ExPhpTypePrimitivePsiImpl].
     */
    private var defaultType: PhpDocTypeImpl? = null

    init {
        val classes = findChildrenByClass(ExPhpTypeInstancePsiImpl::class.java)
        val primitives = findChildrenByClass(ExPhpTypePrimitivePsiImpl::class.java)

        classes.forEach {
            val textBefore = PsiTreeUtil.findSiblingBackward(it, PhpDocTokenTypes.DOC_TEXT, null)
            if (textBefore?.text == ":") {
                extendsClass = it
            } else if (textBefore?.text == "=") {
                defaultType = it
            }
        }

        if (primitives.isNotEmpty()) {
            defaultType = primitives.first()
        }
    }

    override fun getName(): String {
        // TODO: сделать через элементы?
        if (extendsClass != null) {
            return text.substringBefore(':').trim()
        }

        return text.substringBefore('=').trim()
    }

    fun decl(): KphpDocGenericParameterDecl {
        val extendsClassRef = extendsClass?.resolveLocal()?.firstOrNull()
        val fqn = extendsClassRef?.fqn ?: extendsClass?.fqn

        val defaultTypeName = if (defaultType is ExPhpTypeInstancePsiImpl)
            defaultType?.resolveLocal()?.firstOrNull()?.fqn ?: defaultType?.fqn
        else
            defaultType?.name

        return KphpDocGenericParameterDecl(name, fqn, defaultTypeName)
    }
}
