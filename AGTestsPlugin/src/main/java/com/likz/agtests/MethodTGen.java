package com.likz.agtests;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.likz.agtests.config.AutoImport;
import com.likz.agtests.config.ConfigAG;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;

public class MethodTGen extends AnAction {
    private PsiMethod psiMethod;

    public MethodTGen() {
    }

    public MethodTGen(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        if (psiElement == null) psiElement = psiMethod;
        if (psiElement != null && project != null && psiFile != null) {
            // terminal
            TerminalAG terminal = new TerminalAG(project, psiFile.getName());
            terminal.addToRightPanel("Analyzing the method");

            // debugging
            System.out.println(psiElement.getText());

            terminal.finishComponentRightPanel("Analyzing the method");
            // generating text using nn
            INeuroApi connection = new NeuNetConnection(terminal);
            String additionalData = "";
            if (ConfigAG.pluginSettings.getGenerationPattern() != 0)
                additionalData = "pattern=" + ConfigAG.pluginSettings.getGenerationPattern();
            CompletableFuture<String> future = connection.generateTestMethod(psiElement.getText() + "\nclass_name = " + psiFile.getName(), additionalData);

            // updating dependencies
            DependencyFinder.updateDependencies(project, terminal);

            // creating class file (directory if it needs)
            PsiDirectory currentDirectory = TFEngine.createTestPathOfFile(psiFile.getContainingDirectory(), project, terminal);
            String newClassName = psiFile.getName().split("\\.")[0] + "Test";
            PsiClass testClass = TFEngine.createClass(project, currentDirectory, newClassName, terminal);

            // adding method into class
            terminal.finishLeftPanel_1();

            String generatedMethodText = future.join();
            if (generatedMethodText == null) {
                return;
            }
            terminal.addToRightPanel("Test-Method creating");
            PsiMethod newMethod = TFEngine.createMethod(project, generatedMethodText);
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

                System.out.println(newMethod.getText());
                testClass.add(newMethod);
            });
            terminal.finishComponentRightPanel("Test-Method creating");

            // open in new ide window
            terminal.finishLeftPanel_2();
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            fileEditorManager.openFile(testClass.getContainingFile().getVirtualFile());

            AutoImport autoImport = new AutoImport();
            autoImport.addMissingImports(project, testClass.getContainingFile());

        }

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (psiMethod == null)
            e.getPresentation().setVisible(false);
        Editor editor = e.getData(EDITOR);
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        if (psiElement instanceof PsiMethod) {
            PsiMethod currMethod = ((PsiMethod) psiElement);
            e.getPresentation().setVisible(true);

            if (!TFEngine.isItAllowedMethod(currMethod)) {
                e.getPresentation().setEnabled(false);
            }
        }
    }
}
