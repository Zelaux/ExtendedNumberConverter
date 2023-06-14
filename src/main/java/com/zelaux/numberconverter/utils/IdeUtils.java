package com.zelaux.numberconverter.utils;

import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

public class IdeUtils {

    public static class Language {
        public static com.intellij.lang.Language fromVirtualFile(com.intellij.openapi.vfs.VirtualFile file) {
            return LanguageUtil.getFileLanguage(file);
        }

        public static com.intellij.lang.Language from(Editor editor) {
            return LanguageUtil.getFileLanguage(VirtualFile.fromEditor(editor));
        }

        public static com.intellij.lang.Language from(AnActionEvent anActionEvent) {
            return LanguageUtil.getFileLanguage(VirtualFile.fromActionEvent(anActionEvent));
        }
    }

    public static class VirtualFile {
        public static @Nullable com.intellij.openapi.vfs.VirtualFile fromEditor(Editor editor) {
            return FileDocumentManager.getInstance().getFile(editor.getDocument());
        }

        public static @Nullable com.intellij.openapi.vfs.VirtualFile fromActionEvent(AnActionEvent anActionEvent) {
            return anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        }
    }

    public static class PsiFile {
        public static com.intellij.psi.PsiFile from(Project project, com.intellij.openapi.vfs.VirtualFile virtualFile) {
            return PsiManager.getInstance(project).findFile(virtualFile);
        }

        public static com.intellij.psi.PsiFile from(AnActionEvent anActionEvent) {
            return from(projectFromActionEvent(anActionEvent), VirtualFile.fromActionEvent(anActionEvent));
        }
        public static com.intellij.psi.PsiFile from(Editor editor) {
            return IdeUtils.PsiFile.from(editor.getProject(),IdeUtils.VirtualFile.fromEditor(editor));
        }
    }

    public static Project projectFromActionEvent(AnActionEvent anActionEvent) {
        return anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
    }
}
