package com.zelaux.numberconverter.extensions.radix;

import com.intellij.lang.Language;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScalaRadix implements RadixNumberTypeProvider {
    public static final NumberType binary = new RadixNumberTypeImpl(
            "binary", Pattern.compile("(Integer)[\\s\n" +
            "]*(\\.)[\\s\n" +
            "]*(parseInt)[\\s\n" +
            "]*\\([\\s\n" +
            "]*(\"[01]+\")[\\s\n" +
            "]*(,)[\\s\n" +
            "]*(2)\\)[\\s\n" +
            "]*"), "Integer.parseInt(\"", "\", 2)", 2
    ) {
        public final Pattern binaryPatter = Pattern.compile("[01]+");

        @Override
        public BigInteger parse(String value, Language language) {
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
    public @Nullable NumberType binary() {
        return binary;
    }
}
