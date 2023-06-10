package com.zelaux.numberconverter.extensions.bitshift;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.zelaux.numberconverter.actions.ConvertAction;
import com.zelaux.numberconverter.extensionpoints.NumberTypeProvider;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BitShiftAction extends ConvertAction {
    public BitShiftAction() {
        super(null);
//        getTemplatePresentation().setText("As Shifted Bits");
    }

    @Override
    @Nullable
    protected NumberType getType(Language language, @NotNull AnActionEvent anActionEvent) {
        List<NumberTypeProvider> providers = NumberTypeProvider.LANG_EP.allForLanguage(language);
        for (NumberTypeProvider provider : providers) {
            if (provider instanceof BitShiftNumberTypeProvider) {
                BitShiftNumberTypeProvider prov = (BitShiftNumberTypeProvider) provider;
                anActionEvent.getPresentation().setEnabled(true);
                return prov.left;
            }
        }
        anActionEvent.getPresentation().setEnabled(false);

        return null;
    }
}
