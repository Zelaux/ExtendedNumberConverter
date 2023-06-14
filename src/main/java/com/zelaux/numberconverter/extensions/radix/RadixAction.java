package com.zelaux.numberconverter.extensions.radix;

import com.intellij.lang.Language;
import com.zelaux.numberconverter.actions.ConvertAction;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;

import java.util.function.Function;

public abstract class RadixAction extends ConvertAction {
    final Function<RadixNumberTypeProvider, NumberType> mapper;

    public RadixAction(DefaultRadixNumberType defaultType, Function<RadixNumberTypeProvider, NumberType> mapper) {
        super(defaultType);
        this.mapper = mapper;
    }

    @Override
    protected NumberType getType(Language language) {
        RadixNumberTypeProvider provider = RadixNumberTypeProvider.LANG_EP.forLanguage(language);
        if (provider == null) return super.getType(language);
        NumberType apply = mapper.apply(provider);
        if (apply == null) {
//            anActionEvent.getPresentation().setEnabled(false);
            return null;
        }
//        anActionEvent.getPresentation().setEnabled(true);
        return apply;
    }

    public static class DecicalConvertAction extends RadixAction {
        public DecicalConvertAction() {
            super(DefaultRadixNumberType.Decimal, RadixNumberTypeProvider::decimal);
        }

    }

    public static class HexadecimalConvertAction extends RadixAction {
        public HexadecimalConvertAction() {
            super(DefaultRadixNumberType.Hexadecimal, RadixNumberTypeProvider::hexadecimal);
        }
    }

    public static class OctalConvertAction extends RadixAction {
        public OctalConvertAction() {
            super(DefaultRadixNumberType.Octal, RadixNumberTypeProvider::octal);
        }
    }

    public static class BinaryConvertAction extends RadixAction {
        public BinaryConvertAction() {
            super(DefaultRadixNumberType.Binary, RadixNumberTypeProvider::binary);
        }
    }
/*
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        return convertActions;
    }*/
}
