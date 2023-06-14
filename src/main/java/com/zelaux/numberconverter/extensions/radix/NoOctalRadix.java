package com.zelaux.numberconverter.extensions.radix;

import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.Nullable;

public class NoOctalRadix implements RadixNumberTypeProvider {
    @Override
    public @Nullable NumberType.RadixType octal() {
        return null;
    }
}
