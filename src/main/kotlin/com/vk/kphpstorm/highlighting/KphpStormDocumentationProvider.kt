package com.vk.kphpstorm.highlighting

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocVariable
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.PsiToExPhpType
import com.vk.kphpstorm.helpers.toExPhpType
import java.lang.Integer.min

/**
 * My own documentation provider, especially for mouse hover: print only small amount of actual info
 * and (most important) inferred types in expected human-readable form.
 *
 * It works before native
 * @see com.jetbrains.php.lang.documentation.PhpDocumentationProvider
 * And overrides some methods, others are invoked by native, because here (by default) they return null
 */
class KphpStormDocumentationProvider : DocumentationProvider {

    private fun createHoverDocOverride(element: PsiElement, hoverElement: PsiElement): HoverDocOverride? = when (element) {
        is Function                -> PhpFunctionHoverDoc(element, hoverElement)
        is Field                   -> PhpClassFieldHoverDoc(element)
        is Variable                -> PhpVariableHoverDoc(element, hoverElement)
        is PhpDocVariable          -> PhpVariableHoverDoc(element, hoverElement)
        is PhpClass                -> PhpClassHoverDoc(element)
        is Parameter               -> PhpParameterHoverDoc(element, hoverElement)
        is PhpDefine               -> PhpDefineHoverDoc(element)
        is PhpDocType              -> PhpDocTypeHoverDoc(element)
        is StringLiteralExpression -> PhpStringHoverDoc(element)
        else                       -> null
    }

    // when hovering with cmd — very short, one line info
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String? {
        val generator = createHoverDocOverride(element, originalElement)
                ?: return "Don't know what to hint, but can't disable :)"
        return generator.getNavigateDesc()
    }

    // when just hovering — a bit more detailed (but still only most important) info
    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        val generator = createHoverDocOverride(element, originalElement ?: element)
                ?: return "Don't know what to hint, but can't disable :)"
        return "<pre>" + generator.getHoverDesc() + "</pre>"
    }


    interface HoverDocOverride {
        fun getHoverDesc(): String
        fun getNavigateDesc(): String

        fun ExPhpType?.asHtml(expr: PhpPsiElement) =
                if (this == null) "<i>untyped</i> " else "<i>" + toHumanReadable(expr) + "</i> "

        fun String.asName(withDollar: Boolean = false) =
                (if (withDollar) "<b>$" else "<b>") + this + "</b>"

        fun String?.asDefVal() =
                if (this == null || this.isEmpty()) "" else " = $this "
    }

    /**
     * When hovering a function, show its parameters and return type
     */
    private class PhpFunctionHoverDoc(private val f: Function, private val hoverElement: PsiElement) : HoverDocOverride {
        override fun getHoverDesc(): String {
            val parameters = f.parameters
            val lengths = parameters.map { PsiToExPhpType.getArgumentDeclaredType(it, f.project).asHtml(f).length }
            val padL = min(30, lengths.maxOrNull() ?: 0)
            val argsHtml =
                    if (parameters.isEmpty()) "()"
                    else parameters.joinToString("\n", "(\n", "\n)") {
                        val argType = PsiToExPhpType.getArgumentDeclaredType(it, f.project)
                        "  " + argType.asHtml(f).padEnd(padL) + (if (it.isVariadic) "..." else "") + it.name.asName(true) + it.defaultValuePresentation.asDefVal()
                    }

            return kindStr + f.name.asName() + argsHtml + ": " + getReturnType()
        }

        override fun getNavigateDesc(): String {
            val argsHtml =
                    if (f.parameters.isEmpty()) "()"
                    else f.parameters.joinToString(", ", "(", ")") {
                        val argType = PsiToExPhpType.getArgumentDeclaredType(it, f.project)
                        argType.asHtml(f) + (if (it.isVariadic) "..." else "") + it.name.asName(true) + if (it.isOptional) " (optional)" else ""
                    }

            return kindStr + f.name.asName() + argsHtml + ": " + getReturnType()
        }

        private val kindStr
            get() = if (f is Method) f.containingClass?.name + "::" else "function "

        private fun getReturnType() = when (val curUsage = hoverElement.parent) {
            is PhpTypedElement -> curUsage.type.toExPhpType(f.project).asHtml(f)
            else               -> f.type.toExPhpType(f.project).asHtml(f)
        }
    }

    /**
     * When hovering a class reference, show small info about this class
     */
    private class PhpClassHoverDoc(private val klass: PhpClass) : HoverDocOverride {
        override fun getHoverDesc(): String {
            var nConst = 0
            var nStaticFields = 0
            var nInstanceFields = 0
            for (field in klass.ownFields)
                when {
                    field.isConstant         -> nConst++
                    field.modifier.isDynamic -> nInstanceFields++
                    else                     -> nStaticFields++
                }

            return kindStr + klass.name.asName() + extendsStr +
                    " {\n" +
                    nConst.of("constant") +
                    nInstanceFields.of("field") +
                    nStaticFields.of("static field") +
                    klass.ownMethods.size.of("method") +
                    "}"
        }

        override fun getNavigateDesc(): String {
            return kindStr + klass.name.asName() + extendsStr
        }

        private val kindStr
            get() = if (klass.isInterface) "interface " else if (klass.isTrait) "trait " else "class "

        private val extendsStr: String
            get() = run {
                val superName = klass.superName
                val interfaceNames = klass.interfaceNames.joinToString { PhpLangUtil.toShortName(it) }
                when {
                    superName == null && interfaceNames.isEmpty()    -> ""
                    superName != null && interfaceNames.isEmpty()    -> ": $superName"
                    superName == null && interfaceNames.isNotEmpty() -> ": $interfaceNames"
                    else                                             -> ": $superName, $interfaceNames"
                }
            }

        private fun Int.of(singleEngS: String) = when (this) {
            0    -> ""
            1    -> "  1 $singleEngS\n"
            else -> "  $this ${singleEngS}s\n"
        }
    }

    /**
     * When hovering a class field, show its type and default value
     */
    private class PhpClassFieldHoverDoc(private val field: Field) : HoverDocOverride {
        override fun getHoverDesc() = when {
            field.isConstant -> {
                "const " + field.containingClass?.name + "::" + field.name.asName() + field.defaultValuePresentation.asDefVal()
            }
            else             -> {
                val fieldType = PsiToExPhpType.getFieldDeclaredType(field, field.project)
                fieldType.asHtml(field) + field.containingClass?.name + "::" + field.name.asName(true) + field.defaultValuePresentation.asDefVal()
            }
        }

        override fun getNavigateDesc() = getHoverDesc()
    }

    /**
     * When hovering local variable, show its inferred type in this exact place
     */
    private class PhpVariableHoverDoc(private val variable: PhpNamedElement, private val hoverElement: PsiElement) : HoverDocOverride {
        override fun getHoverDesc(): String {
            val hoverVar = hoverElement.parent
            val inplaceType = if (hoverVar is PhpTypedElement) hoverVar.type.toExPhpType(variable.project) else null
            return inplaceType.asHtml(variable) + variable.name.asName(true)
        }

        override fun getNavigateDesc() = getHoverDesc()
    }

    /**
     * When hovering a parameter or its usage, show its type and default value
     * But smartcasts and re-assigns can change its type in this exact place — if so, also output
     */
    private class PhpParameterHoverDoc(private val parameter: Parameter, private val hoverElement: PsiElement) : HoverDocOverride {
        override fun getHoverDesc(): String {
            val project = parameter.project
            val paramType = PsiToExPhpType.getArgumentDeclaredType(parameter, project)
            val hoverVar = hoverElement.parent
            val inplaceType = if (hoverVar is PhpTypedElement) hoverVar.type.toExPhpType(project) else paramType
            return "param " + paramType.asHtml(parameter) + parameter.name.asName(true) + parameter.defaultValuePresentation.asDefVal() +
                    if (inplaceType.toString() == paramType.toString()) "" else "\ncasted to " + inplaceType.asHtml(parameter)
        }

        override fun getNavigateDesc() = getHoverDesc()
    }

    /**
     * When hovering a define, show its value
     */
    private class PhpDefineHoverDoc(private val p: PhpDefine) : HoverDocOverride {
        override fun getHoverDesc(): String {
            val name = p.name
            // these keywords are also detected as defines, showing anything is dummy
            // but if we just return null from our provider, standard will be invoked, it will be even worse
            if (name == "null" || name == "false" || name == "true")
                return "<i>$name</i>"

            return "define " + name.asName() + p.valuePresentation.asDefVal()
        }

        override fun getNavigateDesc() = getHoverDesc()
    }

    /**
     * When hovering a doc type, show it (don't know what to do better)
     */
    private class PhpDocTypeHoverDoc(private val docType: PhpDocType) : HoverDocOverride {
        override fun getHoverDesc(): String {
            var uppermost = docType
            while (uppermost.parent is PhpDocType)
                uppermost = uppermost.parent as PhpDocType

            return uppermost.type.toExPhpType(docType.project).asHtml(docType)
        }

        override fun getNavigateDesc() = getHoverDesc()
    }

    /**
     * When hovering a string, detect pattern $arr['string'] and show detailed type
     * Whyever, PhpStorm triggers hover only on 'strings_with_underscore', not on simple 'strings'
     */
    private class PhpStringHoverDoc(private val string: StringLiteralExpression) : HoverDocOverride {
        override fun getHoverDesc(): String {
            val arrayAccess = PsiTreeUtil.getParentOfType(string, ArrayAccessExpression::class.java)
            if (arrayAccess != null) {
                val itemType = arrayAccess.type.toExPhpType(string.project)
                val lhsStr = if (arrayAccess.value is Variable) "$" + arrayAccess.value!!.name else "..."
                return itemType.asHtml(string) + ("$lhsStr['${string.contents}']").asName()
            }
            return "<i>string</i>"
        }

        override fun getNavigateDesc() = getHoverDesc()
    }
}
