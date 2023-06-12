package com.zelaux.numberconverter.actions;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.Result;
import com.zelaux.numberconverter.numbertype.PsiResult;
import com.zelaux.numberconverter.utils.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConvertAction extends AnAction {

    private final NumberType type;

    @Override
    public final @NotNull ActionUpdateThread getActionUpdateThread() {
        //https://github.com/krasa/StringManipulation/issues/182
        //Access is allowed from event dispatch thread only exception is thrown from MyEditorAction.findActiveSpeedSearchTextField in IntelliJ 2022.3
        return ActionUpdateThread.EDT;
    }

    public ConvertAction(NumberType type) {
        super();

        this.type = type;
    }

    protected static class PsiElementAtCater {
        public static PsiElement element;
        public static int inElementStart, inElementEnd;

        public static void reset() {
            element = null;
            inElementStart = -1;
            inElementEnd = -1;
        }

        public static boolean trySet(PsiFile psiFile, Language language, Caret caret) {
            int selectionStart = caret.getSelectionStart();
            int selectionEnd = caret.getSelectionEnd()-1;
            PsiElement element;
            element = PsiUtil.getCommonPsi(psiFile, language, selectionStart, selectionEnd);
            if (element == null) return false;
            PsiElementAtCater.element=element;
            inElementStart=0;
            inElementEnd=element.getTextLength();
//            inElementStart = selectionStart - element.getTextOffset();
//            inElementEnd = selectionEnd - element.getTextOffset();
            return true;
        }

    }

    public Result<NumberContainer, String> parseNumber(PsiFile file, Caret caret, Language language) {

        if (!PsiElementAtCater.trySet(file, language, caret)) {
            String message = "(Cannot find target)";
            return new Result.Error<>(message);
        }
        PsiElement element = PsiElementAtCater.element;
        int inElementStart = PsiElementAtCater.inElementStart;
        int inElementEnd = PsiElementAtCater.inElementEnd;
        PsiElementAtCater.reset();
        try {
            return new Result.Success<>(NumberContainer.create(element, inElementStart, inElementEnd, language));
        } catch (NumberFormatException e) {
            String message = "(Cannot parse '" + element.getText() + "')";
            return new Result.Error<>(message);
        }
    }


    private Result<NumberContainer, String> findError(PsiFile file, @NotNull List<Caret> caretList, Language language) {

        Result<NumberContainer, String> value = null;

        for (Caret caret : caretList) {
            value = parseNumber(file, caret, language);
            PsiElementAtCater.reset();
            if (value.isError()) {
                return value;
            }
        }

        return value;
    }

    protected NumberType getType(Language language, @NotNull AnActionEvent anActionEvent) {
        return type;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);

        final Document document = editor.getDocument();

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        final Language language = LanguageUtil.getFileLanguage(virtualFile);

        final NumberType numberType = getType(language, anActionEvent);
        if (numberType == null) return;
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);

        CaretModel caretModel = editor.getCaretModel();
        List<Caret> caretList = caretModel.getAllCarets();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            for (Caret caret : caretList) {//caret.getLeadSelectionPosition()
                ;

//                selectionStart-=
                Result<NumberContainer, String> convertedNumber = parseNumber(psiFile, caret, language);

                NumberContainer container = convertedNumber.unwrap();

                caret.removeSelection();
                if (container != null) {
                    PsiResult replacement = container.transformPsiElement(numberType);
                    int elementOffset = container.element.getTextOffset();
                    int beginOffset = container.inElementStart + elementOffset;
                    int endOffset = container.inElementEnd + elementOffset;
                    document.replaceString(beginOffset,endOffset,replacement.text());
//                    convertedNumber.replace(replacement);
//                    int textOffset = replacement.getTextOffset();
                    caret.setSelection(beginOffset, beginOffset + replacement.textLength());
                } else {
                    caret.moveToOffset(caret.getSelectionStart());
                }


            }
        });
    }


    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        super.update(anActionEvent);

        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final VirtualFile virtualFile = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        final Language language = LanguageUtil.getFileLanguage(virtualFile);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

        List<Caret> caretList = editor.getCaretModel().getAllCarets();


        anActionEvent.getPresentation().setEnabledAndVisible(!caretList.isEmpty());
        if (!anActionEvent.getPresentation().isVisible())
            return;
        final NumberType numberType = getType(language, anActionEvent);
        if (numberType == null) return;

        Result<NumberContainer, String> error = findError(psiFile, caretList, language);
        StringBuilder text = new StringBuilder(numberType.title());
        if (!error.isError()) {
            if (caretList.size() == 1) {
                //noinspection DataFlowIssue
                text.append(" (").append(error.unwrap().transformPsiElement(numberType).text()).append(")");
            } else {
                text.append(" (carets ").append(caretList.size()).append(")");
            }
        } else {
            text.append(": ").append(error.getError());
        }

        anActionEvent.getPresentation().setText(text.toString());
        anActionEvent.getPresentation().setEnabled(!error.isError());
    }
}
