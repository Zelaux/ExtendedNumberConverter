package com.zelaux.numberconverter.actions;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.ParsedNumber;
import com.zelaux.numberconverter.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

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

    public Result<ParsedNumber, String> parseNumber(String value, Language language) {
        try {
            return new Result.Success<>(new ParsedNumber(value, language));
        } catch (NumberFormatException e) {
            String message = "(Cannot parse '" + value + "')";
            return new Result.Error<>(message);
        }
    }

    @Nullable
    private String findError(@NotNull List<Caret> caretList, Language language) {

        Result<ParsedNumber, String> value = null;

        for (Caret caret : caretList) {
            value = parseNumber(caret.getSelectedText(), language);
            if (value.isError()) {
                return value.getError();
            }
        }

        return null;
    }

    private List<Caret> FilterCaretWithSelection(@NotNull List<Caret> caretList) {
        return caretList.stream().filter(Caret::hasSelection).collect(Collectors.toList());
    }

    protected NumberType getType(Language language, @NotNull AnActionEvent anActionEvent) {
        return type;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);

        final Document document = editor.getDocument();

        final Language language = LanguageUtil.getFileLanguage(FileDocumentManager.getInstance().getFile(document));

        final NumberType numberType = getType(language, anActionEvent);
        if (numberType == null) return;
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);

        CaretModel caretModel = editor.getCaretModel();
        List<Caret> caretList = FilterCaretWithSelection(caretModel.getAllCarets());

        WriteCommandAction.runWriteCommandAction(project, () ->
                caretList.forEach(caret -> {

                    int selectionStart = caret.getSelectionStart();
                    int selectionEnd = caret.getSelectionEnd();
                    Result<ParsedNumber, String> convertedNumber = parseNumber(caret.getSelectedText(), language);
                    ParsedNumber parsedNumber = convertedNumber.unwrap();
                    if (parsedNumber != null) {
                        document.replaceString(selectionStart, selectionEnd, parsedNumber.toString(numberType, language));
                    }

                    caret.removeSelection();
                    caret.moveToOffset(selectionStart);

                })
        );
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        super.update(anActionEvent);

        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Language language = LanguageUtil.getFileLanguage(anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE));

        List<Caret> caretList = FilterCaretWithSelection(editor.getCaretModel().getAllCarets());


        anActionEvent.getPresentation().setEnabledAndVisible(!caretList.isEmpty());
        if (!anActionEvent.getPresentation().isVisible())
            return;
        final NumberType numberType = getType(language, anActionEvent);
        if (numberType == null) return;
        @Nullable
        String error = findError(caretList, language);
        StringBuilder text = new StringBuilder(numberType.toString());
        if (error == null) {
            if (caretList.size() == 1) {
                text.append(" (" + new ParsedNumber(caretList.get(0).getSelectedText(), language).toString(numberType, language) + ")");
            } else {
                text.append(" (carets " + caretList.size() + ")");
            }
        } else {
            text.append(": " + error);
        }

        anActionEvent.getPresentation().setText(text.toString());
        anActionEvent.getPresentation().setEnabled(error == null);
    }
}
