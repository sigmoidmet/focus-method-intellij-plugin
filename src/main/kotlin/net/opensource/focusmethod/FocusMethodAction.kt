package net.opensource.focusmethod

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldingModel
import com.intellij.psi.PsiManager
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.uast.*

// 15:15
class FocusMethodAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val file: UFile = PsiManager.getInstance(project).findFile(editor.virtualFile)?.toUElementOfType<UFile>() ?: return
        val userCaretOffset = editor.caretModel.currentCaret.offset
        val methods: Map<String, UMethod> = file.classes
            .flatMap { it.methods.toList() }
            .associateBy { it.name }

        val focusMethod: UMethod = file
            .classes
            .flatMap { it.methods.toList() }
            .firstOrNull { it.startOffset <= userCaretOffset && userCaretOffset <= it.endOffset } ?: return

        val dependantMethodNames: Set<String> = findCalledMethodNames(focusMethod.uastBody, methods)
        val methodsForFolding: List<UMethod> = file
            .classes
            .flatMap { it.methods.toList() }
            .filter { !dependantMethodNames.contains(it.name) }
            .filter { it.startOffset > userCaretOffset || userCaretOffset > it.endOffset }
        val foldingModel: FoldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
            methodsForFolding.forEach {
                (foldingModel.getFoldRegion(it.startOffset, it.endOffset) ?:
                foldingModel.addFoldRegion(it.startOffset, it.endOffset, "folded due to focus mode"))
                    ?.isExpanded = false
            }
        }
    }

    private fun findCalledMethodNames(uastBody: UExpression?,
                                      methods: Map<String, UMethod>,
                                      visited: MutableSet<String> = mutableSetOf()) : Set<String> {
        if (uastBody == null || uastBody !is UBlockExpression) {
            return emptySet()
        }
        visited.add((uastBody.uastParent as UMethod).name)


        return uastBody.expressions
            .asSequence()
            .filterIsInstance<UCallExpression>()
            .mapNotNull { it.methodName }
            .filterNot { visited.contains(it) }
            .flatMap { findCalledMethodNames(methods[it]?.uastBody, methods, visited) + it }
            .toSet()
    }
}