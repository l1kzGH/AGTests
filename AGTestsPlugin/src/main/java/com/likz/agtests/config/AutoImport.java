package com.likz.agtests.config;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;

public class AutoImport {

    private static final String[] STATIC_IMPORTS_TO_ADD = {
            "org.junit.jupiter.api.Assertions",
            "org.mockito.Mockito"
    };

    private static final String[] IMPORTS_TO_ADD = {
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.api.Assertions",
            "org.mockito.Mockito",
            "org.mockito.junit.jupiter.MockitoExtension"
    };

    public void addMissingImports(Project project, PsiFile psiFile) {
        System.out.println(psiFile.getName() + " importing depens");
        WriteCommandAction.runWriteCommandAction(project, () -> {
            CodeStyleManager.getInstance(project).reformat(psiFile);

            PsiJavaFile javaFile = (PsiJavaFile) psiFile;
            PsiImportList importList = javaFile.getImportList();
            if (importList == null) {
                return;
            }

            PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
            for (String importToAdd : IMPORTS_TO_ADD) {
                PsiClass importClass = findClass(importToAdd, project, javaFile);
                if (importClass != null) {
                    importList.add(factory.createImportStatement(importClass));
                }
            }

            for (String staticImportToAdd : STATIC_IMPORTS_TO_ADD) {
                PsiClass importClass = findClass(staticImportToAdd, project, javaFile);
                if (importClass != null) {
                    PsiElement importStatement = factory.createImportStaticStatement(importClass, "*");
                    importList.add(importStatement);
                }
            }

            CodeStyleManager.getInstance(project).reformat(psiFile);

            /*
            CodeStyleSettings settings = CodeStyleSettings.getDefaults();
            JavaCodeStyleSettings javaSettings  = settings.getCustomSettings(JavaCodeStyleSettings.class);
            PackageEntryTable table = javaSettings.IMPORT_LAYOUT_TABLE;

            table.addEntry(PackageEntry.ALL_OTHER_IMPORTS_ENTRY);
            table.addEntry(PackageEntry.BLANK_LINE_ENTRY);

            CodeStyleSettingsManager.getInstance(project).setMainProjectCodeStyle(settings);
            */
        });
    }

    private PsiClass findClass(String qualifiedName, Project project, PsiFile context) {
        PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
        PsiClass[] classes = cache.getClassesByName(qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1), GlobalSearchScope.allScope(project));
        for (PsiClass clazz : classes) {
            if (qualifiedName.equals(clazz.getQualifiedName())) {
                return clazz;
            }
        }
        return null;
    }

}