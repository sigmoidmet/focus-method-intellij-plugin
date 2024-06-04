package net.opensource.focusmethod

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldingModel
import com.intellij.psi.PsiManager
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.uast.UFile
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

// 15:15
class FocusMethodAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val file: UFile = PsiManager.getInstance(project).findFile(editor.virtualFile)?.toUElementOfType<UFile>() ?: return
        val userCaretOffset = editor.caretModel.currentCaret.offset
        val focusMethod: UMethod = file
            .classes
            .flatMap { it.methods.toList() }
            .firstOrNull { it.startOffset <= userCaretOffset && userCaretOffset <= it.endOffset } ?: return
    println(focusMethod.name + " " + focusMethod.startOffset + " " + focusMethod.endOffset)
        val methodsForFolding: List<UMethod> = file
            .classes
            .flatMap { it.methods.toList() }
            .filter { it.startOffset > userCaretOffset || userCaretOffset > it.endOffset }

        val foldingModel: FoldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
            methodsForFolding.forEach {
                foldingModel.addFoldRegion(it.startOffset, it.endOffset, "folded")?.isExpanded = false
            }
        }

        editor.foldingModel.run {  }

    }
}