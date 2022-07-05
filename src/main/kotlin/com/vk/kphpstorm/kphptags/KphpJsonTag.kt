package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeArray
import com.vk.kphpstorm.exphptype.ExPhpTypeNullable
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.*

object KphpJsonTag : KphpDocTag("@kphp-json") {

    data class Property(
        val name: String,
        val allowField: Boolean = false,
        val allowClass: Boolean = false,
        val combinedFlatten: Boolean = false,
        val booleanValue: Boolean = false,
        val allowValues: List<String>? = null,
        val ifType: Pair<((ExPhpType?) -> Boolean), String>? = null,
    )

    private val isArray: (ExPhpType?) -> Boolean = {
        val type = if (it is ExPhpTypeNullable) {
            it.inner
        } else {
            it
        }

        if (type is ExPhpTypePipe) {
            type.items.all { itemType -> itemType is ExPhpTypeArray }
        } else {
            type is ExPhpTypeArray
        }
    }

    val properties = listOf(
        Property("rename_policy", allowClass = true, allowValues = listOf("none", "snake_case", "camelCase")),
        Property("visibility_policy", allowClass = true, allowValues = listOf("all", "public")),
        Property("flatten", allowClass = true, combinedFlatten = true, booleanValue = true),
        Property("rename", allowField = true, allowValues = listOf()),
        Property("skip_if_default", allowClass = true, allowField = true, booleanValue = true),
        Property("required", allowField = true, booleanValue = true),
        Property("fields", allowClass = true, allowValues = listOf()),
        Property("skip", allowField = true, booleanValue = true, allowValues = listOf("encode", "decode")),
        Property(
            "raw_string",
            allowField = true,
            combinedFlatten = true,
            booleanValue = true,
            ifType = Pair({ it == ExPhpType.STRING }, "string")
        ),
        Property(
            "float_precision",
            allowClass = true,
            combinedFlatten = true,
            allowField = true,
            allowValues = listOf(),
        ),
        Property(
            "array_as_hashmap",
            allowField = true,
            combinedFlatten = true,
            booleanValue = true,
            ifType = Pair(isArray, "array"),
        ),
    )

    override val description: String
        get() = "Used to change the decoding and encoding behavior of JSON"

    override val elementType: KphpDocTagElementType
        get() = KphpDocElementTypes.kphpDocTagJson

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Field || owner is PhpClass
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        if (owner is Field && owner.modifier.isStatic) {
            return false
        }

        return true
    }

    override fun areDuplicatesAllowed(): Boolean {
        return true
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        val owner = rhs?.parentDocComment?.owner as? PhpNamedElement ?: return

        var forName: String? = null
        val psiElement = if (rhs is KphpDocJsonForEncoderPsiImpl) {
            forName = rhs.name()
            PsiTreeUtil.skipWhitespacesForward(rhs)
        } else {
            rhs
        }

        val propertyPsi = psiElement as? KphpDocJsonPropertyPsiImpl ?: return
        val property = properties.firstOrNull { it.name == propertyPsi.name() }

        when (owner) {
            is Field -> {
                val fieldName = owner.name
                if (owner.modifier.isStatic) {
                    return holder.errTag(docTag, "@kphp-json is allowed only for instance fields: $$fieldName")
                }

                val phpClass = owner.containingClass ?: return
                if (property == null) {
                    return holder.errTag(
                        docTag,
                        "Unknown @kphp-json tag '${propertyPsi.name()}' over class field ${phpClass.name}::$$fieldName"
                    )
                }

                if (!checkPropertyValue(propertyPsi, property, docTag, holder)) {
                    return
                }

                if (!property.allowField) {
                    return holder.errTag(
                        docTag,
                        "@kphp-json tag '${property.name}' is not applicable for the field ${phpClass.name}::$$fieldName"
                    )
                }

                val isUsedCorrectType = property.ifType?.first?.invoke(owner.type.toExPhpType())
                if (isUsedCorrectType == false && propertyPsi.booleanValue() == true) {
                    return holder.errTag(
                        docTag,
                        "field ${phpClass.name}::$$fieldName is @kphp-json '${property.name}', but it's not a ${property.ifType.second}"
                    )
                }

                if (!checkFloatPrecision(propertyPsi, property, docTag, holder)) {
                    return
                }

                if (!checkFlatten(phpClass, property, docTag, holder)) {
                    return
                }

                checkDuplicated(forName, property, docTag, owner, holder)
            }
            is PhpClass -> {
                val className = owner.name

                if (property == null) {
                    return holder.errTag(
                        docTag,
                        "Unknown @kphp-json tag '${propertyPsi.name()}' above class $className"
                    )
                }

                if (forName != null) {
                    val propertyName = property.name
                    var tagJsonPsiElement = docTag as PhpPsiElement?
                    while (tagJsonPsiElement != null) {
                        if (tagJsonPsiElement !is KphpDocTagJsonPsiImpl) {
                            tagJsonPsiElement = tagJsonPsiElement.nextPsiSibling
                            continue
                        }

                        if (tagJsonPsiElement.item()?.name() == propertyName && tagJsonPsiElement.forElement() == null) {
                            return holder.errTag(
                                docTag,
                                "@kphp-json for $forName '$propertyName' should be placed below @kphp-json '$propertyName' without for"
                            )
                        }

                        tagJsonPsiElement = tagJsonPsiElement.nextPsiSibling
                    }
                }

                if (property.name == "fields") {
                    val classInstanceFields = owner.fields.filter { !it.modifier.isStatic }.map { "$${it.name}" }
                    for (item in psiElement.children) {
                        if (item.text !in classInstanceFields) {
                            return holder.errElement(
                                item,
                                "@kphp-json 'fields' specifies '${item.text}', but such field doesn't exist in class $className"
                            )
                        }
                    }
                }

                if (!checkPropertyValue(propertyPsi, property, docTag, holder)) {
                    return
                }

                if (!property.allowClass) {
                    return holder.errTag(
                        docTag,
                        "@kphp-json tag '${property.name}' is not applicable for the class $className"
                    )
                }

                if (!checkFloatPrecision(propertyPsi, property, docTag, holder)) {
                    return
                }

                if (!checkFlatten(owner, property, docTag, holder)) {
                    return
                }

                if (property.name == "flatten" && propertyPsi.booleanValue() == true) {
                    if (forName != null) {
                        return holder.errTag(docTag, "@kphp-json 'flatten' can't be used with 'for', it's a global state")
                    }

                    val classInstanceFields = owner.fields.filter { !it.modifier.isStatic }
                    if (classInstanceFields.size != 1) {
                        return holder.errTag(
                            docTag,
                            "@kphp-json 'flatten' class '$className' must have exactly one field"
                        )
                    }
                }

                checkDuplicated(forName, property, docTag, owner, holder)
            }
        }
    }

    private fun checkDuplicated(
        forName: String?,
        property: Property,
        docTag: PhpDocTag,
        owner: PhpNamedElement,
        holder: AnnotationHolder
    ): Boolean {
        val otherJsonTags = findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(owner)
        if (otherJsonTags.count { it.item()?.name() == property.name && it.forElement()?.name() == forName } > 1) {
            holder.errTag(docTag, "@kphp-json '${property.name}' is duplicated")
            return false
        }

        return true
    }

    private fun checkPropertyValue(
        propertyPsi: KphpDocJsonPropertyPsiImpl,
        property: Property,
        docTag: PhpDocTag,
        holder: AnnotationHolder
    ): Boolean {
        val elementValue = propertyPsi.stringValue()

        val allowValues = property.allowValues
        val isBooleanProperty = property.booleanValue

        if (allowValues == null) {
            if (isBooleanProperty && propertyPsi.booleanValue() == null) {
                holder.errTag(docTag, "@kphp-json '${property.name}' should be empty or true|false, got '${elementValue}'")
                return false
            }

            return true
        }

        if (isBooleanProperty) {
            val newAllowValues = allowValues.toMutableList()
            newAllowValues.addAll(listOf("true", "false"))

            if (elementValue !in newAllowValues && propertyPsi.booleanValue() == null) {
                holder.errTag(
                    docTag,
                    "@kphp-json '${property.name}' should be empty or ${newAllowValues.joinToString("|")}, got '${elementValue}'"
                )
                return false
            }
        } else {
            if (elementValue == null || elementValue.isEmpty()) {
                holder.errTag(docTag, "@kphp-json '${property.name}' expected value")
                return false
            }

            if (elementValue !in allowValues && allowValues.isNotEmpty()) {
                holder.errTag(docTag, "@kphp-json '${property.name}' should be ${allowValues.joinToString("|")}, got '$elementValue'")
                return false
            }
        }

        return true
    }

    private fun checkFlatten(
        phpClass: PhpClass,
        property: Property,
        docTag: PhpDocTag,
        holder: AnnotationHolder,
    ): Boolean {
        if (phpClass.docComment == null || property.combinedFlatten) return true

        val otherJsonTags = findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(phpClass)

        val useFlatten = otherJsonTags.any { it.item()?.name() == "flatten" && it.item()?.booleanValue() == true }
        if (useFlatten) {
            holder.errTag(docTag, "@kphp-json '${property.name}' over a field is disallowed for flatten class '${phpClass.name}'")
            return false
        }

        return true
    }

    private fun checkFloatPrecision(
        propertyPsi: KphpDocJsonPropertyPsiImpl,
        property: Property,
        docTag: PhpDocTag,
        holder: AnnotationHolder
    ): Boolean {
        val isUsedCorrectFloatPrecision = (propertyPsi.intValue() ?: -1) >= 0
        if (property.name == "float_precision" && !isUsedCorrectFloatPrecision) {
            holder.errTag(
                docTag,
                "@kphp-json 'float_precision' value should be non negative integer, got '${propertyPsi.stringValue()}'"
            )
            return false
        }

        return true
    }
}
