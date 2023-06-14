package com.zelaux.numberconverter.extensions.bitshift;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.Result;
import com.zelaux.numberconverter.actions.ConvertAction;
import com.zelaux.numberconverter.actions.ExecutionResult;
import com.zelaux.numberconverter.extensionpoints.NumberTypeProvider;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.utils.IdeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BitShiftAction extends ConvertAction {
    public BitShiftAction() {
        super(null);
        setupHandler(new ConvertActionHandler(this) {
            {
                this.canRunNotForAllCaret = true;

            }

            @Override
            protected boolean isEnabledForCaret(@NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {

                Result<NumberContainer, String> parseNumber = parseNumber(IdeUtils.PsiFile.from(editor), caret, IdeUtils.Language.from(editor));
                if (parseNumber.isError()) return false;
                if (parseNumber.unwrap().parse().getLowestSetBit() == 0) return false;
                return true;
            }
        });
//        getTemplatePresentation().setText("As Shifted Bits");
    }

    @Override
    @Nullable
    protected NumberType getType(Language language) {
        List<NumberTypeProvider> providers = NumberTypeProvider.LANG_EP.allForLanguage(language);
        for (NumberTypeProvider provider : providers) {
            if (provider instanceof BitShiftNumberTypeProvider) {
                BitShiftNumberTypeProvider prov = (BitShiftNumberTypeProvider) provider;
                return prov.left;
            }
        }

        return null;
    }
}
