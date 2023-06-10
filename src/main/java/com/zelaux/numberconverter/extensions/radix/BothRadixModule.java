package com.zelaux.numberconverter.extensions.radix;

import com.intellij.lang.Language;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class BothRadixModule implements RadixNumberTypeProvider {
    public static final NumberType octal = new RadixNumberTypeImpl("octal",
            Pattern.compile("0o?[0-7]"), "Oo", 8
    ) {
        public final Pattern octalPattern = Pattern.compile("[01]+");

        @Override
        public BigInteger parse(String value, Language language) {
            if (value.startsWith("Oo") || value.startsWith("-Oo")) {

                return super.parse(value, language);
            } else {

                if (value.startsWith("-")) {
                    return new BigInteger(value.substring(2)).negate();
                } else {
                    return new BigInteger(value.substring(1));
                }
            }
        }
    };

    @Override
    public @Nullable NumberType octal() {
        return octal;
    }
}
