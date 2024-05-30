package net.opensource.focusmethod

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project


class EditorIllustrationAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.project!!
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.setText("Hello World!")
        }

    }
}