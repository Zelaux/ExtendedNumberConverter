package com.zelaux.numberconverter.extensionpoints;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.regex.Pattern;

public interface RadixNumberTypeProvider {

    ExtensionPointName<LanguageExtensionPoint> EP_NAME = ExtensionPointName.create("com.zelaux.numberconverter.radixNumberTypeProvider");
    LanguageExtension<RadixNumberTypeProvider> LANG_EP = new LanguageExtension<>(EP_NAME.getName());

    @Nullable
    default NumberType hexadecimal() {
        return DefaultRadixNumberType.hexadecimal;
    }

    @Nullable
    default NumberType octal() {
        return DefaultRadixNumberType.octal;
    }

    @Nullable
    default NumberType decimal() {
        return DefaultRadixNumberType.decimal;
    }

    @Nullable
    default NumberType binary() {
        return DefaultRadixNumberType.binary;
    }

    class RadixNumberTypeImpl implements NumberType {
        public final String name;
        public final Pattern pattern;
        public final String prefix;
        public final String postfix;
        public final int radix;
        public final boolean isDecimal;

        public RadixNumberTypeImpl(String name, Pattern pattern, String prefix, int radix) {
            this(name, pattern, prefix, "", radix);
        }

        public RadixNumberTypeImpl(String name, Pattern pattern, String prefix, String postfix, int radix) {
            this.name = name;
            this.pattern = Pattern.compile("-?" + pattern.pattern());
            this.prefix = prefix;
            this.postfix = postfix;
            this.radix = radix;
            isDecimal = radix == 10;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean match(String value, Language language) {
            return pattern.matcher(value).matches();
        }

        @Override
        public BigInteger parse(String value, Language language) {
            if (value.charAt(0) == '-') {
                value = '-' + value.substring(prefix.length() + 1,value.length()-postfix.length());
            } else {
                value = value.substring(prefix.length(),value.length()-postfix.length());

            }
            return new BigInteger(value, radix);
        }

        @Override
        public String wrap(BigInteger integer, Language language) {

            if (integer.signum() < 0 && isDecimal) {
                int bitCount = integer.bitCount();
                int p2 = Integer.highestOneBit(bitCount - 1) * 2;

                p2 = Math.max(p2, 16);
//            int p2 = 16;
                if (p2 > 16) p2 = 32;
//            if (p2 > 32) p2 = 64;
                integer = integer.add(BigInteger.ONE.shiftLeft(p2 * 2));
            }
            return prefix + integer.toString(radix).toUpperCase() + postfix;
        }
    }

}
