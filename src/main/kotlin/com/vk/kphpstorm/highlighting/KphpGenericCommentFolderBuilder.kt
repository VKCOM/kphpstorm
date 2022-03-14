package com.vk.kphpstorm.highlighting

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.vk.kphpstorm.generics.psi.GenericInstantiationPsiCommentImpl

class KphpGenericCommentFolderBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup("KPHP Generic")
        val descriptors = mutableListOf<FoldingDescriptor>()

        root.accept(object : PhpElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is GenericInstantiationPsiCommentImpl) {
                    descriptors.add(
                        FoldingDescriptor(
                            element.node,
                            TextRange(
                                element.textRange.startOffset,
                                element.textRange.endOffset
                            ),
                            group
                        )
                    )
                }

                var child = element.firstChild
                while (child != null) {
                    child.accept(this)
                    child = child.nextSibling
                }
            }
        })

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val text = node.text
        return text.substring(2, text.length - 2)
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
}
