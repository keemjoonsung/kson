package com.joonsung.kson;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class SavePathDialog extends DialogWrapper {

    private final Project project;
    private final VirtualFile file;
    private TextFieldWithBrowseButton textFieldWithBrowseButton;
    private JTextField classNameField;
    private JLabel errorLabel;

    protected SavePathDialog(@Nullable Project project, VirtualFile file) {
        super(project);
        this.project = project;
        this.file = file;
        setTitle("Save Generated Class");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel errorPanel = new JPanel(new BorderLayout());

        String fileName = file.getNameWithoutExtension();
        String defaultClassName = Character.toUpperCase(fileName.charAt(0)) + fileName.substring(1);

        classNameField = new JTextField(defaultClassName);
        textFieldWithBrowseButton = new TextFieldWithBrowseButton();
        textFieldWithBrowseButton.setText(file.getParent().getPath() + "/" + defaultClassName + ".java");

        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);

        classNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateFilePath();
                validateInput();
            }
        });

        textFieldWithBrowseButton.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileSaverDescriptor("Save Generated Class", "Choose a directory to save the generated class file", "class");
            VirtualFile baseDir = project.getBaseDir();
            VirtualFile saveFile = FileChooser.chooseFile(descriptor, project, baseDir);

            if (saveFile != null) {
                updateFilePath(saveFile.getPath());
                validateInput();
            }
        });

        topPanel.add(new JLabel("Class name:"), BorderLayout.WEST);
        topPanel.add(classNameField, BorderLayout.CENTER);

        centerPanel.add(new JLabel("Save to directory:"), BorderLayout.WEST);
        centerPanel.add(textFieldWithBrowseButton, BorderLayout.CENTER);

        errorPanel.add(errorLabel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(errorPanel, BorderLayout.SOUTH);

        Dimension preferredSize = new Dimension(600, panel.getPreferredSize().height);
        panel.setPreferredSize(preferredSize);
        validateInput();
        return panel;
    }

    private void updateFilePath() {
        String className = classNameField.getText();
        String directoryPath = textFieldWithBrowseButton.getText().substring(0, textFieldWithBrowseButton.getText().lastIndexOf('/'));
        textFieldWithBrowseButton.setText(directoryPath + "/" + className + ".java");
    }

    private void updateFilePath(String directoryPath) {
        String className = classNameField.getText();
        textFieldWithBrowseButton.setText(directoryPath + "/" + className + ".java");
    }

    private void validateInput() {
        String filePath = textFieldWithBrowseButton.getText();
        String className = classNameField.getText();

        File file = new File(filePath);
        if (className.isEmpty()) {
            getOKAction().setEnabled(false);
        }
        else if (file.exists()) {
            errorLabel.setText("Class name already exists.");
            getOKAction().setEnabled(false);
        } else {
            errorLabel.setText("");
            getOKAction().setEnabled(true);
        }
    }

    public String getSavePath() {
        return textFieldWithBrowseButton.getText();
    }

    public String getClassName() {
        String className = Character.toUpperCase(classNameField.getText().charAt(0)) + classNameField.getText().substring(1);;
        return className;
    }
}