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
        val allowValues: List<String>? = null,
        val ifType: Pair<((ExPhpType?) -> Boolean), String>? = null,
    )

    private val isArray: (ExPhpType?) -> Boolean = {
        val type = if (it is ExPhpTypeNullable) {
            it.inner
        } else {
            it
        }

        type is ExPhpTypeArray
    }

    val properties = listOf(
        Property("fields_rename", allowClass = true, allowValues = listOf("none", "snake_case", "camelCase")),
        Property("fields_visibility", allowClass = true, allowValues = listOf("all", "public")),
        Property("flatten", allowClass = true, combinedFlatten = true),
        Property("rename", allowField = true, allowValues = listOf()),
        Property("skip_if_default", allowClass = true, allowField = true),
        Property("required", allowField = true),
        Property("raw_string", allowField = true, ifType = Pair({ it == ExPhpType.STRING }, "string")),
        Property("skip", allowField = true),
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
        val elementValue = propertyPsi.stringValue()

        if (property != null) {
            val allowValues = property.allowValues
            if (allowValues == null) {
                if (elementValue != null) {
                    return holder.errTag(docTag, "@kphp-json '${property.name}' not expected value")
                }
            } else {
                if (elementValue == null || elementValue.isEmpty()) {
                    return holder.errTag(docTag, "@kphp-json '${property.name}' expected value")
                }

                if (elementValue !in allowValues && allowValues.isNotEmpty()) {
                    return holder.errTag(
                        docTag,
                        "@kphp-json '${property.name}' should be either ${allowValues.joinToString("|")}"
                    )
                }
            }
        }

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

                if (property.name == "skip") {
                    val otherJsonTags = findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(owner)

                    if (otherJsonTags.size > 1) {
                        return holder.errTag(
                            docTag,
                            "@kphp-json 'skip' can't be used together with other @kphp-json tags"
                        )
                    }
                }

                checkFlatten(phpClass, property, docTag, holder)
            }
            is PhpClass -> {
                val className = owner.name

                if (property == null) {
                    return holder.errTag(
                        docTag,
                        "Unknown @kphp-json tag '${propertyPsi.name()}' above class $className"
                    )
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

                if (property.name == "flatten" && owner.fields.size != 1) {
                    return holder.errTag(
                        docTag,
                        "@kphp-json 'flatten' tag is allowed only for class with a single field, class name $className"
                    )
                }
            }
        }
    }

    private fun checkFlatten(
        phpClass: PhpClass,
        property: Property,
        docTag: PhpDocTag,
        holder: AnnotationHolder,
    ): Boolean {
        if (phpClass.docComment == null || property.combinedFlatten) return true

        val otherJsonTags = findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(phpClass)

        val useFlatten = otherJsonTags.any { it.item()?.name() == "flatten" }
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
        val isUsedCorrectFloatPrecision = (propertyPsi.intValue() ?: 0) > 0
        if (property.name == "float_precision" && !isUsedCorrectFloatPrecision) {
            holder.errTag(
                docTag,
                "@kphp-json 'float_precision' value should be non negative integer, got '${propertyPsi.intValue()}'"
            )
            return false
        }

        return true
    }
}
