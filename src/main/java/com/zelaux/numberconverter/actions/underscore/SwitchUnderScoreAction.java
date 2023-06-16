package com.zelaux.numberconverter.actions.underscore;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.util.IntPair;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.actions.ExecutionResult;
import com.zelaux.numberconverter.exceptions.MyException;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.utils.IdeUtils;
import com.zelaux.numberconverter.utils.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.zelaux.numberconverter.actions.MyEditorAction;
import com.zelaux.numberconverter.actions.MyEditorWriteActionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SwitchUnderScoreAction extends MyEditorAction {

    protected SwitchUnderScoreAction() {
        super(null);
        setupHandler(new MyEditorWriteActionHandler<List<CaretDescriptor>>(this) {

            @Override
            protected @NotNull ExecutionResult<List<CaretDescriptor>> beforeWriteAction(Editor editor, DataContext dataContext) {
                ArrayList<CaretDescriptor> list = new ArrayList<>();
                for (Caret caret : editor.getCaretModel().getAllCarets()) {
                    Language language = IdeUtils.Language.from(editor);
                    NumberContainer container = getNumberContainer(editor, caret, language);
                    PsiElement element = container.element;
                    DefaultRadixNumberType defaultRadixNumberType =
                            NumberType.getRadixTypes(language).collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey))
                                    .get(container.currentSystem());
                    NumberType.RadixType radixType = (NumberType.RadixType) container.currentSystem();
                    int spacing = defaultRadixNumberType.underScoreSpacing();
                    IntPair range = radixType.numberContentRange(container, 0, container.element.getTextLength());
                    if (Math.abs(range.first - range.second) <= spacing) continue;
                    String text = container.element.getText();
                    IntPair destination = new IntPair(element.getTextOffset(), element.getTextOffset() + element.getTextLength());
                    list.add(CaretDescriptor.pool.obtain().set(range, text, destination, spacing, radixType));
                }
                return new ExecutionResult.Content<>(list);
            }

            @Override
            protected boolean isEnabledForCaret(@NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
                Language language = IdeUtils.Language.from(editor);
                NumberContainer container = getNumberContainer(editor, caret, language);
                if (container == null) return false;
                NumberType.RadixType radixType = (NumberType.RadixType) container.currentSystem();
                IntPair range = radixType.numberContentRange(container, 0, container.element.getTextLength());
                if (range == null) return false;
                return true;

            }

            @Override
            protected void executeWriteAction(Editor editor, DataContext dataContext, List<CaretDescriptor> additionalParameter) {
                Document document = editor.getDocument();
                for (CaretDescriptor descriptor : additionalParameter) {
                    IntPair range = descriptor.range;
                    String text = descriptor.text.substring(Math.min(range.first, range.second), Math.max(range.first, range.second));
                    String clearText = text.replace("_", "");
                    int spacing = descriptor.spacing;
                    int prevIndex = 0;
                    StringBuilder builder = new StringBuilder((clearText.length() / spacing + 1) * spacing);
                    if (range.second > range.first || true) {
                        for (int i = clearText.length() % spacing; i <= clearText.length(); i += spacing) {
                            builder.append(clearText, prevIndex, i);
                            if (i + 1 < clearText.length() && i > 0) builder.append('_');
                            prevIndex = i;
                        }
                    } else {

                    }
                    String targetText;
                    if (builder.toString().equals(text)) {
                        targetText = clearText;
                    } else {
                        targetText = builder.toString();
                    }
                    document.replaceString(descriptor.destination.first, descriptor.destination.second,
                            descriptor.radixType.wrapUnderScore(targetText)
                    );
                    descriptor.free();
                }
            }
        });
    }


    @Nullable
    private static NumberContainer getNumberContainer(@NotNull Editor editor, @NotNull Caret caret, Language language) {
        PsiUtil.CommonPsiAndRanges element = PsiUtil.getCommonPsiAndRanges(IdeUtils.PsiFile.from(editor), language, caret.getSelectionStart(), caret.getSelectionEnd());
        if (element == null) return null;
        try {
            NumberContainer container = NumberContainer.createOrNull(
                    element.element, element.inElementStart, element.inElementEnd, language, NumberType.getRadixTypes(language).map(Map.Entry::getValue)
            );
            return container;
        } catch (Exception e) {
            throw new MyException(element.element,element.inElementStart,element.inElementEnd,language,e);
        }
    }

}
