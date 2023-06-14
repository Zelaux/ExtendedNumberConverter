package com.zelaux.numberconverter.extensions.radix;

import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class BothRadixModule implements RadixNumberTypeProvider {
    public static final NumberType.RadixType octal = new RadixNumberTypeImpl(DefaultRadixNumberType.Octal,
            Pattern.compile("0o?[0-7]"), "0o"
    ) {

        @Override
        public BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd) {
            String value = container.getText(inElementStart, inElementEnd);
            if (value.startsWith("Oo") || value.startsWith("-Oo")) {
                return super.parse(container, inElementStart, inElementEnd);
            } else {

                if (value.startsWith("-")) {
                    return new BigInteger(value.substring(2),8).negate();
                } else {
                    return new BigInteger(value.substring(1),8);
                }
            }
        }
    };

    @Override
    public @Nullable NumberType.RadixType octal() {
        return octal;
    }
}
