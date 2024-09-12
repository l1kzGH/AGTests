package com.likz.agtests.gutter;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class GIVChanging implements PsiTreeChangeListener {

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        System.out.println("* Class redacting *");
        PsiFile file = event.getFile();
        if(file != null){
            Project project = file.getProject();
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            VirtualFile virtualFile = editor.getVirtualFile();
            if (virtualFile != null) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);

                if (psiClass != null) {
                    Document document = editor.getDocument();
                    MarkupModel markupModel = editor.getMarkupModel();
                    markupModel.removeAllHighlighters();

                    for (PsiMethod method : psiClass.getMethods()) {
                        GutterIconGenerator.addGutterIconForMethod(method, markupModel, document);
                    }
                }
            }
        }
    }

    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

    }
}
