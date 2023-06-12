package com.zelaux.numberconverter.numbertype;

import com.zelaux.numberconverter.NumberContainer;
import org.intellij.lang.annotations.Language;

import java.math.BigInteger;
import java.util.regex.Pattern;

public enum DefaultRadixNumberType implements
        NumberType,
        NumberType.MatchByPattern {
    binary("Binary", "\\s*0[bB](_*[01])*\\s*", "0b", 2),
    octal("Octal", "\\s*0[0-7](_*[0-7])*\\s*", "0", 8),
    decimal("Decimal", "\\s*(0|[1-9](_*[0-9])*)\\s*", "", 10),
    hexadecimal("Hex", "\\s*0[xX][0-9a-fA-F](_*[0-9a-fA-F])*\\s*", "0x", 16),
    ;
    public final String title;
    public final Pattern pattern;
    public final String prefix;
    public final int radix;


    DefaultRadixNumberType(
            String title, @Language("REGEXP")
    String pattern, String prefix, int radix) {
        this.title = title;
        this.pattern = Pattern.compile(pattern);
        this.prefix = prefix;
        this.radix = radix;
    }


    @Override
    public String toString() {
        return getClass().getCanonicalName() + "#" + name();
    }


    @Override
    public String title() {
        return title;
    }

    @Override
    public BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd) {

        String value = container.getText(inElementStart, inElementEnd).replace("_", "").trim();
        if (value.charAt(0) == '-') {
            value = '-' + value.substring(prefix.length() + 1);
        } else {
            value = value.substring(prefix.length());

        }
        return new BigInteger(value, radix);
    }

    @Override
    public PsiResult wrap(NumberContainer container, BigInteger integer) {

        if (integer.signum() < 0 && this != decimal) {
            int bitCount = integer.bitCount();
            int p2 = Integer.highestOneBit(bitCount - 1) * 2;

            p2 = Math.max(p2, 16);
//            int p2 = 16;
            if (p2 > 16) p2 = 32;
//            if (p2 > 32) p2 = 64;
            integer = integer.add(BigInteger.ONE.shiftLeft(p2 * 2));
        }
        return container.psiFromText(prefix + integer.toString(radix).toUpperCase());
    }

    @Override
    public Pattern pattern() {
        return pattern;
    }
}
