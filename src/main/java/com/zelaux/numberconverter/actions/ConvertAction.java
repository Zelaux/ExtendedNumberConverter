package com.zelaux.numberconverter.actions;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.exceptions.MyException;
import com.zelaux.numberconverter.highlight.MyHighlightManager;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.Result;
import com.zelaux.numberconverter.numbertype.PsiResult;
import com.zelaux.numberconverter.utils.IdeUtils;
import com.zelaux.numberconverter.utils.MyFormatUtil;
import com.zelaux.numberconverter.utils.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConvertAction extends MyEditorAction {

    private final NumberType type;


    public ConvertAction(NumberType type) {
        super(null);
        setupHandler(new ConvertActionHandler(this));

        this.type = type;
    }

    protected class ConvertActionHandler extends MyEditorWriteActionHandler<@NotNull NumberType> {
        public ConvertActionHandler(AnAction owner) {
            super(owner);
        }

        @Override
        protected @NotNull ExecutionResult<NumberType> beforeWriteAction(Editor editor, DataContext dataContext) {
            NumberType numberType = getType(IdeUtils.Language.from(editor));
            if (numberType == null) return stopExecution();
            return new ExecutionResult.Content<>(numberType);
        }

        @Override
        protected boolean isEnabledForCaret(@NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
            NumberType numberType = getType(IdeUtils.Language.from(editor));
            if (numberType == null) return false;
            return true;
            /*
            Result<NumberContainer, String> result = parseNumber(IdeUtils.PsiFile.from(editor),
                    caret, IdeUtils.Language.from(editor)
            );
            if(result.isError())return false;

            return super.isEnabledForCaret(editor, caret, dataContext);*/
        }

        @Override
        protected void executeWriteAction(Editor editor, DataContext dataContext, NumberType additionalParameter) {
            writeAction(editor, dataContext, additionalParameter);
        }
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
            int selectionEnd = caret.getSelectionEnd();
            PsiUtil.CommonPsiAndRanges element = PsiUtil.getCommonPsiAndRanges(psiFile, language, selectionStart, selectionEnd);
            if (element == null) return false;
            PsiElementAtCater.element = element.element;
            inElementStart = element.inElementStart;
            inElementEnd = element.inElementEnd;
//            inElementStart = selectionStart - element.getTextOffset();
//            inElementEnd = selectionEnd - element.getTextOffset();
            return true;
        }

    }

    public Result<NumberContainer, String> parseNumber(PsiFile file, Caret caret, Language language) {

        PsiElement element=null;
        int inElementStart=-1;
        int inElementEnd=-1;
        try {
            if (!PsiElementAtCater.trySet(file, language, caret)) {
                String message = "Cannot find target";
                return new Result.Error<>(message);
            }
            element = PsiElementAtCater.element;
            inElementStart = PsiElementAtCater.inElementStart;
            inElementEnd = PsiElementAtCater.inElementEnd;
            PsiElementAtCater.reset();
            try {
                return new Result.Success<>(NumberContainer.create(element, inElementStart, inElementEnd, language));
            } catch (NumberFormatException e) {
                String message = "Cannot parse '" + element.getText() + "'";
                return new Result.Error<>(message);
            }
        } catch (Exception e) {
            throw new MyException(element,inElementStart,inElementEnd,language,e);
        }
    }


    private Result<NumberContainer, String> findError(PsiFile file, @NotNull List<Caret> caretList, Language language, List<NumberContainer> parsed) {

        Result<NumberContainer, String> value = null;

        for (Caret caret : caretList) {
            value = parseNumber(file, caret, language);
            PsiElementAtCater.reset();
            if (value.isError()) {
                return value;
            } else {
                parsed.add(value.unwrap());
            }
        }

        return value;
    }

    protected NumberType getType(Language language) {
        return type;
    }


    public void writeAction(@NotNull Editor editor, DataContext dataContext, final NumberType numberType) {

        final Document document = editor.getDocument();

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        final Language language = LanguageUtil.getFileLanguage(virtualFile);
        final Project project = editor.getProject();

        CaretModel caretModel = editor.getCaretModel();
        List<Caret> caretList = caretModel.getAllCarets();

       /* WriteCommandAction.runWriteCommandAction(project, () -> {
        });*/
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        for (Caret caret : caretList) {//caret.getLeadSelectionPosition()
            ;

//                selectionStart-=
            Result<NumberContainer, String> convertedNumber = parseNumber(psiFile, caret, language);

            NumberContainer container = convertedNumber.unwrap();

            caret.removeSelection();
            if (container != null) {
                PsiResult replacement = container.transformPsiElement(numberType);
                replacement.apply(document, container);
                int elementOffset = container.element.getTextOffset();
                int beginOffset = container.inElementStart + elementOffset;
//                    convertedNumber.replace(replacement);
//                    int textOffset = replacement.getTextOffset();
                caret.setSelection(beginOffset, beginOffset + replacement.textLength());
            } else {
                caret.moveToOffset(caret.getSelectionStart());
            }


        }

    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        super.update(anActionEvent);

        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
//        final VirtualFile virtualFile = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        final Language language = IdeUtils.Language.from(editor);
//        final Project project = IdeUtils.projectFromActionEvent(anActionEvent);
//        final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

        List<Caret> caretList = editor.getCaretModel().getAllCarets();


        anActionEvent.getPresentation().setEnabledAndVisible(!caretList.isEmpty());
        if (!anActionEvent.getPresentation().isVisible()) {
            return;
        }

        final NumberType numberType = getType(language);
        VirtualFile from = IdeUtils.VirtualFile.from(editor);
        if (numberType == null || from != null && !from.isWritable()) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }

//        editor.getDocument().
        ArrayList<NumberContainer> parsed = new ArrayList<>();
        Result<NumberContainer, String> error = findError(IdeUtils.PsiFile.from(editor), caretList, language, parsed);
        StringBuilder text = new StringBuilder(numberType.title());

        MyHighlightManager myHighlightManager = MyHighlightManager.getInstance(editor);
        if (!error.isError()) {
            for (NumberContainer container : parsed) {
                int offset = container.element.getTextOffset();
                myHighlightManager.addHighlight(offset + container.inElementStart, offset + container.inElementEnd);
            }
            if (caretList.size() == 1) {
                //noinspection DataFlowIssue
                text.append(" '").append(
                        MyFormatUtil.format(error.unwrap().transformPsiElement(numberType).text())
                ).append("'");
            } else {
                text.append(" (carets ").append(caretList.size()).append(")");
            }
        } else {
            text.append(": Error(").append(error.getError() + ")");
        }

        anActionEvent.getPresentation().setText(text.toString());
        anActionEvent.getPresentation().setEnabled(!error.isError());
    }
}
