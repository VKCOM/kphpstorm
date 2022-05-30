package com.vk.kphpstorm

import com.intellij.lang.DefaultASTFactoryImpl
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.tree.IElementType
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl

class KphpStormASTFactory : DefaultASTFactoryImpl() {
    override fun createComment(type: IElementType, text: CharSequence): LeafElement {
        if (text.startsWith("/*<") && text.endsWith(">*/")) {
            return GenericInstantiationPsiCommentImpl(type, text)
        }

        return super.createComment(type, text)
    }
}
