package com.zelaux.numberconverter.extensions.radix;

import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class SecondRadix implements RadixNumberTypeProvider {
    public final NumberType octal = new RadixNumberTypeImpl(
            "octal", Pattern.compile("0o[0-7]"), "0o", 8
    );

    @Override
    public @Nullable NumberType octal() {
        return octal;
    }
}
