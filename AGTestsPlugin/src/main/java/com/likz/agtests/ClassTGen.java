package com.likz.agtests;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.likz.agtests.config.AutoImport;
import com.likz.agtests.config.ConfigAG;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClassTGen extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_FILE);
        if (psiElement != null && project != null) {
            // terminal
            TerminalAG terminal = new TerminalAG(project, psiElement.getContainingFile().getName());

            terminal.addToRightPanel("Method's selection");
            // methods select
            PsiClass psiClass = PsiTreeUtil.getChildOfType(psiElement, PsiClass.class);
            List<PsiMethod> psiMethods = methodsToSelectMenu(psiClass);
            System.out.println(psiMethods);
            if(psiMethods == null || psiMethods.equals(new ArrayList<>())) {
                terminal.errorComponentRightPanel("Method's selection");
                return;
            }

            List<String> methodTexts = new ArrayList<>();
            for (PsiMethod method : psiMethods) {
                methodTexts.add(method.getText() + "\nclass_name = " + psiClass.getName() + ".java");
            }
            terminal.finishComponentRightPanel("Method's selection");

            // generating text using nn
            INeuroApi connection = new NeuNetConnection(terminal);
            String additionalData = "";
            if (ConfigAG.pluginSettings.getGenerationPattern() != 0)
                additionalData = "pattern=" + ConfigAG.pluginSettings.getGenerationPattern();
            CompletableFuture<List<String>> future = connection.generateTestMethods(methodTexts, additionalData);

            // updating dependencies
            DependencyFinder.updateDependencies(project, terminal);

            // creating class file (directory if it needs)
            PsiDirectory currentDirectory = TFEngine.createTestPathOfFile(psiClass.getContainingFile().getContainingDirectory(), project, terminal);
            String newClassName = psiClass.getName().split("\\.")[0] + "Test";
            PsiClass testClass = TFEngine.createClass(project, currentDirectory, newClassName, terminal);

            terminal.finishLeftPanel_1();
            List<String> generatedMethodTexts = future.join();
            if(generatedMethodTexts == null) {
                return;
            }
            // adding method's into class
            terminal.addToRightPanel("Test-Method's creating");
            for (String genMethodText : generatedMethodTexts) {
                PsiMethod newMethod = TFEngine.createMethod(project, genMethodText);
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    int sameMethods = 0;
                    for (PsiMethod method : testClass.getMethods()) {
                        if (method.getName().startsWith(newMethod.getName())) {
                            sameMethods++;
                        }
                    }
                    if (sameMethods > 0) {
                        newMethod.setName(newMethod.getName() + "_v" + sameMethods);
                    }
                    testClass.add(newMethod);
                    CodeStyleManager.getInstance(project).reformat(newMethod);
                });
            }
            terminal.finishComponentRightPanel("Test-Method's creating");

            // open in new ide window
            terminal.finishLeftPanel_2();
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            fileEditorManager.openFile(testClass.getContainingFile().getVirtualFile());

            AutoImport autoImport = new AutoImport();
            autoImport.addMissingImports(project, testClass.getContainingFile());
        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);
        boolean isVisible = psiFile != null && psiFile.getName().endsWith(".java");
        e.getPresentation().setVisible(isVisible);
    }

    //-----

    private List<PsiMethod> methodsToSelectMenu(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.getMethods();
        JCheckBox[] checkBoxes = new JCheckBox[methods.length];
        for (int i = 0; i < methods.length; i++) {
            checkBoxes[i] = new JCheckBox(methods[i].getName());
            if(!TFEngine.isItAllowedMethod(methods[i]))
                checkBoxes[i].setEnabled(false);
        }
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(methods.length, 1));
        for (JCheckBox checkBox : checkBoxes) {
            panel.add(checkBox);
        }
        int option = JOptionPane.showConfirmDialog(null, panel, "Select class methods:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            List<PsiMethod> selectedMethods = new ArrayList<>();
            for (JCheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    PsiMethod method = psiClass.findMethodsByName(checkBox.getText(), false)[0];
                    selectedMethods.add(method);
                }
            }

            return selectedMethods;
        }

        return null;
    }

}