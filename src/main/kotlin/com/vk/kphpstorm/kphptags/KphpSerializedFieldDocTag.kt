package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.helpers.parentDocComment

object KphpSerializedFieldDocTag : KphpDocTag("@kphp-serialized-field") {
    override val description: String
        get() = "[KPHP] Unique serialization index in range of 0..127 within @kphp-serializable class."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is Field && !owner.isConstant && owner.modifier.isDynamic &&
                owner.containingClass.let { it != null && KphpSerializableDocTag.existsInDocComment(it) }
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        val phpClass = PsiTreeUtil.getParentOfType(docComment, PhpClass::class.java) ?: return false
        if (!KphpSerializableDocTag.existsInDocComment(phpClass))
            return false
        return owner == null || owner is Field && !owner.isConstant && owner.modifier.isDynamic
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        if (rhs == null)
            return holder.errTag(docTag, "[KPHP] Specify index or 'none'")

        // rhs must be only-child: numeric index or 'none'
        val text = rhs.text.substringBefore(' ')
        if (text == "none")
            return
        val curIndex = text.toIntOrNull()
        if (curIndex == null || curIndex !in 0..127)
            return holder.errTag(docTag, "[KPHP] Index must be a numeric 0..127")

        // check for duplicates with other @kphp-serialized-field inside the same class
        val curField = docTag.parentDocComment?.owner as? Field ?: return
        val phpClass = curField.containingClass ?: return
        for (field in phpClass.ownFields)
            if (field != curField) {
                val fIdx = parseIndexFromFieldPhpdoc(field)
                if (fIdx == curIndex)
                    holder.errTag(docTag, "[KPHP] Duplicate index with field \$${field.name}")
            }

        // check that curIndex is not listed in @kphp-reserved-tags above class
        val kphpReservedTag = KphpReservedFieldsDocTag.findThisTagInDocComment(phpClass)
        if (kphpReservedTag != null) {
            val reservedIndexes = KphpReservedFieldsDocTag.parseIndexes(PhpDocUtil.getTagValue(kphpReservedTag))
            if (reservedIndexes != null && reservedIndexes.contains(curIndex)) {
                holder.errTag(docTag, "[KPHP] This index is listed in @kphp-reserved-fields")
            }
        }
    }

    override fun onAutoCompleted(docComment: PhpDocComment): String? {
        // do not use docComment.owner here: if we write code below everything (above future var), it will be null
        val phpClass = PsiTreeUtil.getParentOfType(docComment, PhpClass::class.java)
                ?: return null

        // think of index for this field: 1 + maximum of all fields + reserved
        var maxIndex = 0
        for (field in phpClass.ownFields) {
            val fIdx = parseIndexFromFieldPhpdoc(field)
            if (fIdx != null && fIdx > maxIndex)
                maxIndex = fIdx
        }
        val kphpReservedTag = KphpReservedFieldsDocTag.findThisTagInDocComment(phpClass)
        if (kphpReservedTag != null) {
            val reservedIndexes = KphpReservedFieldsDocTag.parseIndexes(PhpDocUtil.getTagValue(kphpReservedTag))
            maxIndex = Integer.max(maxIndex, reservedIndexes?.maxOrNull() ?: 0)
        }

        return (maxIndex + 1).toString()
    }

    fun parseIndexFromFieldPhpdoc(field: Field): Int? {
        val ksfTag = findThisTagInDocComment(field) ?: return null
        val value = PhpDocUtil.getTagValue(ksfTag)
        return value.toIntOrNull()          // 'none' is just null, like any other text
    }
}
