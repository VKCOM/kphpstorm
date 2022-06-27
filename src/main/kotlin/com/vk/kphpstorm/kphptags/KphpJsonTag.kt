package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeArray
import com.vk.kphpstorm.exphptype.ExPhpTypeNullable
import com.vk.kphpstorm.exphptype.ExPhpTypePipe
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocJsonPropertyPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagElementType
import com.vk.kphpstorm.kphptags.psi.KphpDocTagJsonPsiImpl

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
        if (rhs == null) {
            return
        }

        val propertyPsi = rhs as? KphpDocJsonPropertyPsiImpl ?: return
        val property = properties.firstOrNull { it.name == propertyPsi.name() }

        when (val owner = rhs.parentDocComment?.owner) {
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

                if (!checkPropertyValue(propertyPsi, property, holder, docTag)) {
                    return
                }

                if (!property.allowField) {
                    return holder.errTag(
                        docTag,
                        "@kphp-json tag '${property.name}' is not applicable for the field ${phpClass.name}::$$fieldName"
                    )
                }

                val isUsedCorrectType = property.ifType?.first?.invoke(owner.type.toExPhpType())
                if (isUsedCorrectType == false) {
                    return holder.errTag(
                        docTag,
                        "@kphp-json ${property.name} tag is allowed only above ${property.ifType.second} type, got above $$fieldName field"
                    )
                }

                if (!checkFloatPrecision(propertyPsi, property, holder, docTag)) {
                    return
                }

                if (property.name == "skip" && propertyPsi.booleanValue() == true) {
                    val otherJsonTags = findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(owner)

                    if (otherJsonTags.size > 1) {
                        return holder.errTag(
                            docTag,
                            "@kphp-json 'skip' can't be used together with other @kphp-json tags"
                        )
                    }
                }

                if (!checkFlatten(phpClass, property, docTag, holder)) {
                    return
                }

                val otherJsonTags = findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(owner)
                if (otherJsonTags.count { it.item()?.name() == property.name } > 1) {
                    return holder.errTag(docTag, "@kphp-json '${property.name}' is duplicated")
                }
            }
            is PhpClass -> {
                val className = owner.name

                if (property == null) {
                    return holder.errTag(
                        docTag,
                        "Unknown @kphp-json tag '${propertyPsi.name()}' above class $className"
                    )
                }

                if (!checkPropertyValue(propertyPsi, property, holder, docTag)) {
                    return
                }

                if (!property.allowClass) {
                    return holder.errTag(
                        docTag,
                        "@kphp-json tag '${property.name}' is not applicable for the class $className"
                    )
                }

                if (!checkFloatPrecision(propertyPsi, property, holder, docTag)) {
                    return
                }

                if (!checkFlatten(owner, property, docTag, holder)) {
                    return
                }

                if (property.name == "flatten" && owner.fields.size != 1 && propertyPsi.booleanValue() == true) {
                    return holder.errTag(
                        docTag,
                        "@kphp-json 'flatten' tag is allowed only for class with a single field, class name $className"
                    )
                }

                val otherJsonTags = findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(owner)
                if (otherJsonTags.count { it.item()?.name() == property.name } > 1) {
                    return holder.errTag(docTag, "@kphp-json '${property.name}' is duplicated")
                }
            }
        }
    }

    private fun checkPropertyValue(
        propertyPsi: KphpDocJsonPropertyPsiImpl,
        property: Property,
        holder: AnnotationHolder,
        docTag: PhpDocTag
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
                holder.errTag(docTag, "@kphp-json '${property.name}' should be either ${allowValues.joinToString("|")}")
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
            holder.errTag(docTag, "'${property.name}' can't be used for a @kphp-json 'flatten' class")
            return false
        }

        return true
    }

    private fun checkFloatPrecision(
        propertyPsi: KphpDocJsonPropertyPsiImpl,
        property: Property,
        holder: AnnotationHolder,
        docTag: PhpDocTag
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
