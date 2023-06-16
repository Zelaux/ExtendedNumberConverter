package com.zelaux.numberconverter.extensions.activities;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.zelaux.numberconverter.highlight.MyHighlightManager;
import com.zelaux.numberconverter.utils.IdeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InitUpdaterListeners implements StartupActivity {
//    private static final Key<> NUMBER_MANIPULATION_UPDATE_PSI = Key.create("NUMBER_MANIPULATION_UPDATE_PSI");
public class I{

}
    @Override
    public void runActivity(@NotNull Project project) {
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeAnyChangeAbstractAdapter() {

            @Override
            protected void onChange(@Nullable PsiFile file) {
                FileEditorManager instance = FileEditorManager.getInstance(project);
                Editor editor = instance.getSelectedTextEditor();

                if (!IdeUtils.VirtualFile.from(editor).getName().equals(file.getVirtualFile().getName())) {
                    return;
                }
                for (Caret caret : editor.getCaretModel().getAllCarets()) {
//                    caret.getUserData(NUMBER_MANIPULATION_UPDATE_PSI)
                }
            }
        });
    }
}
