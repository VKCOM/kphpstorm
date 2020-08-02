package com.vk.kphpstorm.kphptags

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.vk.kphpstorm.helpers.parentDocComment

object KphpReservedFieldsDocTag : KphpDocTag("@kphp-reserved-fields") {
    override val description: String
        get() = "For serializable classes: you should place here all indexes of deleted fields — to prevent their usage in future."

    override fun isApplicableFor(owner: PsiElement): Boolean {
        return owner is PhpClass && KphpSerializableDocTag.existsInDocComment(owner)
    }

    override fun needsAutoCompleteOnTyping(docComment: PhpDocComment, owner: PsiElement?): Boolean {
        return KphpSerializableDocTag.existsInDocComment(docComment)
    }

    override fun onAutoCompleted(docComment: PhpDocComment): String? {
        return "[|]"
    }

    override fun annotate(docTag: PhpDocTag, rhs: PsiElement?, holder: AnnotationHolder) {
        // check format of rhs: it must be [index1, index2, ...]
        // processing rhs.text is much easier than psi children
        val indexes = parseIndexes(rhs?.text ?: "")
                ?: return holder.errTag(docTag, "Format is: [index1, index2, ...]")

        // check for all @kphp-serialized-field inside the class: that none of them has a removed index
        val phpClass = docTag.parentDocComment?.owner as? PhpClass ?: return
        for (field in phpClass.ownFields) {
            val fIdx = KphpSerializedFieldDocTag.parseIndexFromFieldPhpdoc(field)
            if (fIdx != null && indexes.contains(fIdx))
                return holder.errTag(docTag, "Index $fIdx is used in field \$${field.name}")
        }
    }

    fun parseIndexes(kphpReservedTagArgument: String): List<Int>? {
        val indexes = kphpReservedTagArgument.trim('[', ']').split(',').map { it.trim().toIntOrNull() ?: -1 }
        val allNums = indexes.all { it in 0..127 }
        return if (allNums) indexes else null
    }
}
