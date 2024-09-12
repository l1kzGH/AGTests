package com.likz.agtests;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.likz.agtests.config.ConfigAG;

import java.io.IOException;
import java.util.Stack;

public class TFEngine {

    static PsiDirectory createTestPathOfFile(PsiDirectory psiDirectory, Project project, TerminalAG terminal) {
        terminal.addToRightPanel("Setting up the project directory");
        if (!ConfigAG.pluginSettings.getCustomerDirectory().isEmpty()) {
            System.out.println("custom");
            String path = project.getBasePath() + "/" + ConfigAG.pluginSettings.getCustomerDirectory();
            VirtualFile[] virtualFile = new VirtualFile[1];
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    virtualFile[0] = VfsUtil.createDirectoryIfMissing(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            PsiManager psiManager = PsiManager.getInstance(project);
            terminal.finishComponentRightPanel("Setting up the project directory");
            return psiManager.findDirectory(virtualFile[0]);
        }

        Stack<String> queue = new Stack<>();
        while (!psiDirectory.getName().equals("main")) {
            queue.add(psiDirectory.getName());
            psiDirectory = psiDirectory.getParent();
        }
        psiDirectory = psiDirectory.getParent();
        psiDirectory = psiDirectory.findSubdirectory("test");
        while (!queue.isEmpty()) {
            if (psiDirectory.findSubdirectory(queue.peek()) != null) {
                psiDirectory = psiDirectory.findSubdirectory(queue.pop());
            } else {
                final PsiDirectory temp = psiDirectory;
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    temp.createSubdirectory(queue.peek());
                });
                psiDirectory = psiDirectory.findSubdirectory(queue.pop());
            }
        }
        terminal.finishComponentRightPanel("Setting up the project directory");
        return psiDirectory;
    }

    static PsiClass createClass(Project project, PsiDirectory directory, String className, TerminalAG terminal) {
        terminal.addToRightPanel("Setting up a class in the directory");
        PsiClass[] newClass = new PsiClass[1];
        String[] classNameArr = new String[1];

        if(!ConfigAG.pluginSettings.getCustomerTestName().equals("[class_name]Test")) {
            classNameArr[0] = ConfigAG.pluginSettings.getCustomerTestName().replaceAll("[\\[\\]]", "");
        } else {
            classNameArr[0] = className;
        }
        if (directory.findFile(classNameArr[0] + ".java") == null) {
            WriteCommandAction.runWriteCommandAction(project, () -> {

                newClass[0] = PsiElementFactory.getInstance(project).createClass(classNameArr[0]);
                directory.add(newClass[0]);
            });
        } else {
            Messages.showMessageDialog(
                    "*** The java class already exists ***\n" +
                            "If you continue, there will be changes in the class",
                    "ReCreate", Messages.getInformationIcon());
        }
        PsiFile file = directory.findFile(classNameArr[0] + ".java");
        if (file instanceof PsiJavaFile) {
            newClass[0] = ((PsiJavaFile) file).getClasses()[0];
        } else {
            System.out.println("This is not java class");
        }
        terminal.finishComponentRightPanel("Setting up a class in the directory");
        return newClass[0];
    }

    static PsiMethod createMethod(Project project, String methodText) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        return elementFactory.createMethodFromText(methodText, null);
    }

    @Deprecated
    static PsiFile javaFileCreator(Project project, PsiDirectory directory, String classFile) {
        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        PsiFile[] file = new PsiFile[1];
        WriteCommandAction.runWriteCommandAction(project, () -> {
            // new file
            if (directory.findFile(classFile + ".java") == null) {
                file[0] = factory.createFileFromText(
                        classFile + ".java",
                        JavaFileType.INSTANCE,
                        "\npublic class " + classFile + "{}");
                directory.add(file[0]);
            } else { // add into (nothing now)
                // todo
                file[0] = directory.findFile(classFile + ".java");
                Messages.showMessageDialog(
                        "*** The java class already exists ***\n" +
                                "If you continue, there will be changes in the class",
                        "ReCreate", Messages.getInformationIcon());
            }
        });
        return file[0];
    }

    public static boolean isItAllowedMethod(PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();
        PsiAnnotation[] annotations = method.getAnnotations();

        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName != null && qualifiedName.startsWith("org.junit")) {
                return false;
            }
        }
        if (method.getReturnType() == null) {
            return false;
        }
        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            return false;
        }

        return true;
    }

}