package com.likz.agtests.gutter;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.psi.PsiMethod;
import com.likz.agtests.MethodTGen;
import com.likz.agtests.TFEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URL;

public class GutterIconGenerator {

    public static void addGutterIconForMethod(PsiMethod psiMethod, MarkupModel markupModel, Document document) {
        int offset = psiMethod.getTextOffset();
        Icon icon = AllIcons.RunConfigurations.TestCustom;

        RangeHighlighter highlighter = markupModel.addLineHighlighter(document.getLineNumber(offset), 0, null);

        if (TFEngine.isItAllowedMethod(psiMethod)){
            highlighter.setGutterIconRenderer(new MyGutterIconRenderer(icon, psiMethod));
        } else {
            icon = AllIcons.Actions.DeleteTag;
            highlighter.setGutterIconRenderer(new MyGutterIconRenderer(icon));
        }
    }

    private static class MyGutterIconRenderer extends GutterIconRenderer {
        private final Icon icon;
        private final PsiMethod method;

        public MyGutterIconRenderer(Icon icon) {
            this(icon, null);
        }

        public MyGutterIconRenderer(Icon icon, PsiMethod method) {
            this.icon = icon;
            this.method = method;
        }

        @NotNull
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public Alignment getAlignment() {
            return Alignment.LEFT;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof MyGutterIconRenderer && ((MyGutterIconRenderer) obj).getIcon().equals(getIcon());
        }

        @Override
        public int hashCode() {
            return getIcon().hashCode();
        }

        @Override
        public boolean isNavigateAction() {
            return method != null;
        }

        @Override
        public @Nullable String getTooltipText() {
            if (method != null) {
                return "Create test-method by AutoGen";
            }
            return "Nothing to do";
        }

        @Override
        public @Nullable AnAction getClickAction() {
            if (method != null) {
                return new MethodTGen(method);
            }
            return super.getClickAction();
        }
    }

}
