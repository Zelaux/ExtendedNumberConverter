package com.zelaux.numberconverter.extensionpoints;

import com.intellij.lang.LanguageExtension;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.IntPair;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.PsiResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.regex.Pattern;

public interface RadixNumberTypeProvider {

    ExtensionPointName<LanguageExtensionPoint> EP_NAME = ExtensionPointName.create("com.zelaux.numberconverter.radixNumberTypeProvider");
    LanguageExtension<RadixNumberTypeProvider> LANG_EP = new LanguageExtension<>(EP_NAME.getName());

    @Nullable
    default NumberType.RadixType hexadecimal() {
        return DefaultRadixNumberType.Hexadecimal;
    }

    @Nullable
    default NumberType.RadixType octal() {
        return DefaultRadixNumberType.Octal;
    }

    @NotNull
    default NumberType.RadixType decimal() {
        return DefaultRadixNumberType.Decimal;
    }

    @Nullable
    default NumberType.RadixType binary() {
        return DefaultRadixNumberType.Binary;
    }

    class RadixNumberTypeImpl implements NumberType, NumberType.RadixType, NumberType.MatchByPattern {
        public final DefaultRadixNumberType parentType;
        public final Pattern pattern;
        public final String prefix;
        public final String postfix;
        public final boolean isDecimal;

        @Override
        public String title() {
            return parentType.name();
        }

        public RadixNumberTypeImpl(DefaultRadixNumberType parentType, Pattern pattern, String prefix) {
            this(parentType, pattern, prefix, "");
        }

        public RadixNumberTypeImpl(DefaultRadixNumberType parentType, Pattern pattern, String prefix, String postfix) {
            this.parentType = parentType;
            this.pattern = Pattern.compile("-?" + pattern.pattern());
            this.prefix = prefix;
            this.postfix = postfix;
            isDecimal = parentType.isDecimal();
        }

        @Override
        public Pattern pattern() {
            return pattern;
        }


        @Override
        public BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd) {
            String value = container.getText(inElementStart, inElementEnd);
            if (value.charAt(0) == '-') {
                value = '-' + value.substring(prefix.length() + 1, value.length() - postfix.length());
            } else {
                value = value.substring(prefix.length(), value.length() - postfix.length());

            }
            return new BigInteger(value, parentType.radix);
        }

        @Override
        public PsiResult wrap(NumberContainer container, BigInteger integer) {

            if (integer.signum() < 0 && isDecimal) {
                int bitCount = integer.bitCount();
                int p2 = Integer.highestOneBit(bitCount - 1) * 2;

                p2 = Math.max(p2, 16);
//            int p2 = 16;
                if (p2 > 16) p2 = 32;
//            if (p2 > 32) p2 = 64;
                integer = integer.add(BigInteger.ONE.shiftLeft(p2 * 2));
            }
            return container.psiFromText(prefix + integer.toString(parentType.radix).toUpperCase() + postfix);
        }

        @Override
        public IntPair numberContentRange(NumberContainer container, int inElementStart, int inElementEnd) {
            String text = container.getText(inElementStart, inElementEnd);
            return RadixType.clearWhiteSpaces(text, prefix.length(), postfix.length());
        }

        @Override
        public String wrapUnderScore(String numberWithUnderScore) {
            return prefix + numberWithUnderScore + postfix;
        }
    }

}
