package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.exphptype.ExPhpType
import com.vk.kphpstorm.exphptype.ExPhpTypeArray
import com.vk.kphpstorm.helpers.parentDocComment
import com.vk.kphpstorm.helpers.toExPhpType
import com.vk.kphpstorm.kphptags.psi.KphpDocElementTypes
import com.vk.kphpstorm.kphptags.psi.KphpDocJsonItemPsiImpl
import com.vk.kphpstorm.kphptags.psi.KphpDocTagElementType
import com.vk.kphpstorm.kphptags.psi.KphpDocTagJsonPsiImpl

object KphpJsonTag : KphpDocTag("@kphp-json") {

    data class KphpJsonElement(
        val name: String,
        val allowField: Boolean = false,
        val allowClass: Boolean = false,
        val combinedFlatten: Boolean = false,
        val allowValues: List<String>? = null,
        val ifType: Pair<((ExPhpType?) -> Boolean), String>? = null,
    )

    val jsonElements = listOf(
        KphpJsonElement("fields_rename", allowClass = true, allowValues = listOf("none", "snake_case", "camelCase")),
        KphpJsonElement("fields_visibility", allowClass = true, allowValues = listOf("all", "public")),
        KphpJsonElement("flatten", allowClass = true, combinedFlatten = true),
        KphpJsonElement("rename", allowField = true),
        KphpJsonElement("skip_if_default", allowClass = true, allowField = true, allowValues = listOf("true", "false")),
        KphpJsonElement("required", allowField = true),
        KphpJsonElement("raw_string", allowField = true, ifType = Pair({ it == ExPhpType.STRING }, "string")),
        KphpJsonElement("skip", allowField = true),
        KphpJsonElement(
            "float_precision",
            allowClass = true,
            combinedFlatten = true,
            allowField = true,
            allowValues = listOf(),
        ),
        KphpJsonElement(
            "array_as_hashmap",
            allowField = true,
            combinedFlatten = true,
            ifType = Pair({ it is ExPhpTypeArray }, "array"),
        ),
    )

    override val description: String
        get() = ""

    override val elementType: KphpDocTagElementType
        get() = KphpDocElementTypes.kphpDocTagJson

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Field || owner is PhpClass
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return true
    }

    override fun areDuplicatesAllowed(): Boolean {
        return true
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null) {
            return
        }

        val jsonElement = rhs as? KphpDocJsonItemPsiImpl ?: return

        when (val owner = rhs.parentDocComment?.owner) {
            is Field -> {
                val elementName = jsonElement.name()

                val fieldName = owner.name
                val classElement = owner.containingClass ?: return

                val element =
                    jsonElements.firstOrNull { it.name == elementName && it.allowField } ?: return holder.errTag(
                        docTag,
                        "Unknown @kphp-json tag '$elementName' over class field ${classElement.name}::$$fieldName"
                    )

                if (owner.modifier.isStatic) {
                    return holder.errTag(
                        docTag, "@kphp-json is allowed only for instance fields: $$fieldName"
                    )
                }

                if (element.allowValues != null) {
                    val elementValue = jsonElement.stringValue()
                    if (elementValue == null || elementValue.isEmpty()) {
                        return holder.errTag(docTag, "@kphp-json '$elementName' expected value")
                    }

                    if (elementValue !in element.allowValues && element.allowValues.isNotEmpty()) {
                        return holder.errTag(
                            docTag,
                            "@kphp-json '$elementName' should be either ${element.allowValues.joinToString(separator = "|")}"
                        )
                    }
                }

                val isUsedCorrectType = element.ifType?.first?.invoke(owner.type.toExPhpType())
                if (element.name == elementName && isUsedCorrectType != null && !isUsedCorrectType) {
                    holder.errTag(
                        docTag,
                        "@kphp-json $elementName tag is allowed only above ${element.ifType.second} type, got above $$fieldName field"
                    )
                }

                if (element.name == "float_precision" && (jsonElement.intValue() ?: 0) < 0) {
                    holder.errTag(
                        docTag,
                        "@kphp-json 'float_precision' value should be non negative integer, got '${jsonElement.intValue()}'"
                    )
                }

                if (element.name == "skip") {
                    val otherJsonTags = this.findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(owner)

                    if (otherJsonTags.size > 1) {
                        holder.errTag(docTag, "@kphp-json 'skip' can't be used together with other @kphp-json tags")
                    }
                }

                if (classElement.docComment != null && !element.combinedFlatten) {
                    val otherJsonTags = this.findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(classElement)

                    val useFlatten = otherJsonTags.any { it.item()?.name() == "flatten" }
                    if (useFlatten) {
                        holder.errTag(docTag, "'$elementName' can't be used for a @kphp-json 'flatten' class")
                    }
                }
            }
            is PhpClass -> {
                val elementName = jsonElement.name()

                val className = owner.name

                val element = jsonElements.firstOrNull { it.name == elementName && it.allowClass }
                    ?: return holder.errTag(docTag, "Unknown @kphp-json tag '$elementName' above class $className")

                if (element.allowValues != null) {
                    val elementValue = jsonElement.stringValue()
                    if (elementValue == null || elementValue.isEmpty()) {
                        return holder.errTag(docTag, "@kphp-json '$elementName' expected value")
                    }

                    if (elementValue !in element.allowValues && element.allowValues.isNotEmpty()) {
                        return holder.errTag(
                            docTag,
                            "@kphp-json '$elementName' should be either ${element.allowValues.joinToString(separator = "|")}"
                        )
                    }
                }

                if (element.name == "float_precision" && (jsonElement.intValue() ?: 0) < 0) {
                    holder.errTag(
                        docTag,
                        "@kphp-json 'float_precision' value should be non negative integer, got '${jsonElement.intValue()}'"
                    )
                }

                if (owner.docComment != null && !element.combinedFlatten) {
                    val otherJsonTags = this.findThisTagsInDocComment<KphpDocTagJsonPsiImpl>(owner)

                    val useFlatten = otherJsonTags.any { it.item()?.name() == "flatten" }
                    if (useFlatten) {
                        holder.errTag(docTag, "'$elementName' can't be used for a @kphp-json 'flatten' class")
                    }
                }

                if (elementName == "flatten" && owner.fields.size != 1) {
                    holder.errTag(
                        docTag,
                        "@kphp-json 'flatten' tag is allowed only for class with a single field, class name $className"
                    )
                }
            }
        }
    }
}
