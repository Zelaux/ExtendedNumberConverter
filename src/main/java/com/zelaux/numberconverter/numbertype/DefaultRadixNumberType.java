package com.zelaux.numberconverter.numbertype;

import com.intellij.util.IntPair;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.settings.MySettingsState;
import org.intellij.lang.annotations.Language;

import java.math.BigInteger;
import java.util.regex.Pattern;

public enum DefaultRadixNumberType implements
        NumberType,
        NumberType.MatchByPattern,
        NumberType.RadixType {
    Binary("To Binary", "\\s*0[bB](_*[01])*\\s*", "0b", 2, it -> it.binarySeparator),
    Octal("To Octal", "\\s*0[0-7](_*[0-7])*\\s*", "0", 8, it -> it.octalSeparator),
    Decimal("To Decimal", "\\s*(0|[1-9](_*[0-9])*)\\s*", "", 10, it -> it.decimalSeparator),
    Hexadecimal("To Hex", "\\s*0[xX][0-9a-fA-F](_*[0-9a-fA-F])*\\s*", "0x", 16, it -> it.hexSeparator),
    ;
    public final ToInt<MySettingsState> spacing;
    public final String title;
    public final Pattern pattern;
    public final String prefix;
    public final int radix;

    @SuppressWarnings("StatementWithEmptyBody")
    public IntPair numberContentRange(NumberContainer container, int inElementStart, int inElementEnd) {
        String text = container.getText(inElementStart, inElementEnd);
        return RadixType.clearWhiteSpaces(text, prefix.length(), 0);
    }

    @Override
    public String wrapUnderScore(String numberWithUnderScore) {
        return prefix+numberWithUnderScore;
    }


    interface ToInt<T> {
        int get(T t);
    }

    DefaultRadixNumberType(
            String title, @Language("REGEXP")
    String pattern, String prefix, int radix, ToInt<MySettingsState> spacing) {
        this.title = title;
        this.pattern = Pattern.compile(pattern);
        this.prefix = prefix;
        this.radix = radix;
        this.spacing = spacing;
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

        if (integer.signum() < 0 && this != Decimal) {
            int bitCount = integer.bitCount();
            int p2 = Integer.highestOneBit(bitCount - 1) * 2;

            p2 = Math.max(p2, 16);
//            int p2 = 16;
            if (p2 > 16) p2 = 32;
//            if (p2 > 32) p2 = 64;
            integer = integer.add(BigInteger.ONE.shiftLeft(p2 * 2));
        }
        String process = MySettingsState.getInstance().outputCase.process(integer.toString(radix));
        return container.psiFromText(prefix + process);
    }

    @Override
    public Pattern pattern() {
        return pattern;
    }

    public int underScoreSpacing() {
        return spacing.get(MySettingsState.getInstance());
    }
}
