package com.joonsung.kson;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class GenerateClassAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || file == null) {
            return;
        }

        SavePathDialog dialog = new SavePathDialog(project, file);
        if (dialog.showAndGet()) {
            String savePath = dialog.getSavePath();

            String selectedPath = savePath;

            try {
                ParseJson.generateJavaClass(file, selectedPath, dialog.getClassName());
                Messages.showMessageDialog(project, "Java class generated successfully", "Success", Messages.getInformationIcon());
            } catch (Exception e) {
                Messages.showErrorDialog(project, "Failed to generate Java class: " + e.getMessage(), "Error");
            }

        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean isJsonFile = file != null && file.getName().endsWith(".json");
        event.getPresentation().setEnabledAndVisible(isJsonFile);
    }
}
