package com.zelaux.numberconverter.numbertype;

import com.intellij.lang.Language;

import java.math.BigInteger;
import java.util.regex.Pattern;

public enum DefaultRadixNumberType implements NumberType {
    binary("0[bB][01]+","0b", 2),
    octal("0[0-7]+","0", 8),
    decimal("(0|[1-9][0-9]*)","", 10),
    hexadecimal("0[xX][0-9a-fA-F]+","0x", 16),
    ;
    public final String name = name();
    public final Pattern pattern;
    public final String prefix;
    public final int radix;

    DefaultRadixNumberType(
            @org.intellij.lang.annotations.Language("REGEXP")
            String pattern, String prefix, int radix) {
        this.pattern = Pattern.compile(pattern);
        this.prefix = prefix;
        this.radix = radix;
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
            value = '-' + value.substring(prefix.length() + 1);
        } else {
            value = value.substring(prefix.length());

        }
        return new BigInteger(value, radix);
    }

    @Override
    public String wrap(BigInteger integer, Language language) {

        if (integer.signum() < 0 && this != decimal) {
            int bitCount = integer.bitCount();
            int p2 = Integer.highestOneBit(bitCount - 1) * 2;

            p2 = Math.max(p2, 16);
//            int p2 = 16;
            if (p2 > 16) p2 = 32;
//            if (p2 > 32) p2 = 64;
            integer = integer.add(BigInteger.ONE.shiftLeft(p2 * 2));
        }
        return prefix + integer.toString(radix).toUpperCase();
    }
}
