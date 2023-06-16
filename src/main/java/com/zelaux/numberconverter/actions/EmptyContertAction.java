package com.zelaux.numberconverter.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.zelaux.numberconverter.highlight.MyHighligh;
import com.zelaux.numberconverter.highlight.MyHighlightManager;
import com.zelaux.numberconverter.utils.IdeUtils;
import org.jetbrains.annotations.NotNull;

public class EmptyContertAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = IdeUtils.Editor.from(e);
        MyHighlightManager.getInstance(editor).removeHighlight();
        e.getPresentation().setEnabledAndVisible(false);

    }

}
