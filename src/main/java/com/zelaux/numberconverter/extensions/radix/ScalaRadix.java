package com.zelaux.numberconverter.extensions.radix;

import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScalaRadix implements RadixNumberTypeProvider {
    public static final NumberType.RadixType binary = new RadixNumberTypeImpl(
            DefaultRadixNumberType.Binary, Pattern.compile("(Integer)[\\s\n" +
            "]*(\\.)[\\s\n" +
            "]*(parseInt)[\\s\n" +
            "]*\\([\\s\n" +
            "]*(\"[01]+\")[\\s\n" +
            "]*(,)[\\s\n" +
            "]*(2)\\)[\\s\n" +
            "]*"), "Integer.parseInt(\"", "\", 2)"
    ) {
        public final Pattern binaryPatter = Pattern.compile("[01]+");

        @Override
        public BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd) {
            String value = container.getText(inElementStart, inElementEnd).trim();
            Matcher matcher = binaryPatter.matcher(value);
            matcher.find();
            BigInteger bigInteger = new BigInteger(matcher.group(), 2);
            if (value.startsWith("-")) {
                return bigInteger.negate();
            }
            return bigInteger;
        }
    };

    @Override
    public @Nullable NumberType.RadixType binary() {
        return binary;
    }
}
