package com.zelaux.numberconverter.extensions.bitshift;

import com.intellij.lang.Language;
import com.zelaux.numberconverter.actions.ConvertAction;
import com.zelaux.numberconverter.extensionpoints.NumberTypeProvider;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AsOrProductAction extends ConvertAction {
    public AsOrProductAction() {
        super(null);
//        getTemplatePresentation().setText("As Shifted Bits");
    }

    @Override
    @Nullable
    protected NumberType getType(Language language) {
        List<NumberTypeProvider> providers = NumberTypeProvider.LANG_EP.allForLanguage(language);
        for (NumberTypeProvider provider : providers) {
            if (provider instanceof BitShiftNumberTypeProvider) {
                BitShiftNumberTypeProvider prov = (BitShiftNumberTypeProvider) provider;
                return prov.or;
            }
        }

        return null;
    }
}
