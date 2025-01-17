package com.zelaux.numberconverter.extensions.radix;

import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class SecondRadix implements RadixNumberTypeProvider {
    public final NumberType.RadixType octal = new RadixNumberTypeImpl(
            DefaultRadixNumberType.Octal, Pattern.compile("0o[0-7]"), "0o"
    );

    @Override
    public @Nullable NumberType.RadixType octal() {
        return octal;
    }
}
