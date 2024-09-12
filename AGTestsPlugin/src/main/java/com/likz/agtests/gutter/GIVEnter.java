package com.likz.agtests.gutter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.likz.agtests.config.ConfigAG;
import com.likz.agtests.config.PluginSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class GIVEnter implements EditorFactoryListener {

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(ConfigAG.pluginDataDir);
        if (file.exists())
            try {
                String json = objectMapper.readValue(file, String.class);
                ConfigAG.pluginSettings = objectMapper.readValue(json, PluginSettings.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        System.out.println("Reading settings");
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        System.out.println("* Entered a Class *");
        Editor editor = event.getEditor();
        VirtualFile virtualFile = editor.getVirtualFile();
        if (virtualFile != null) {
            PsiFile psiFile = PsiManager.getInstance(editor.getProject()).findFile(virtualFile);
            PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);

            Document document = editor.getDocument();
            MarkupModel markupModel = editor.getMarkupModel();

            if (psiClass != null)
                for (PsiMethod method : psiClass.getMethods()) {
                    GutterIconGenerator.addGutterIconForMethod(method, markupModel, document);
                }
        }
    }
}