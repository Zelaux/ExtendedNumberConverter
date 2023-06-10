package com.zelaux.numberconverter.extensions.bitshift;

import com.intellij.lang.Language;
import com.zelaux.numberconverter.numbertype.NumberType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KotlinBitShift extends BitShiftNumberTypeProvider {
    public static Pattern pattern = Pattern.compile("\\s*\\d+\\s*(<<|>>|>>>)\\s*\\d+\\s*");
    public static Pattern center = Pattern.compile("(<<|>>|>>>)");
    private static final String[] bitShiftTypeString;
    private static final Pattern[] bitShiftTypePattern;

    static {
        BitShiftType[] values = BitShiftType.values();
        bitShiftTypeString = new String[values.length];
        bitShiftTypePattern = new Pattern[values.length];
        for (int i = 0; i < values.length; i++) {
            switch (values[i]) {

                case leftShift:
                    bitShiftTypeString[i] = " shl";
                    break;
                case rightShift:
                    bitShiftTypeString[i] = "shr";
                    break;
                case unsignedRightShift:
                    bitShiftTypeString[i] = "ushr";
                    break;
            }
            bitShiftTypePattern[i] = Pattern.compile("\\s*(" + bitShiftTypeString[i] + ")\\s*");
        }
    }

    @Override
    public boolean match(String value, BitShiftType bitShiftType, Language language) {
        Matcher matcher = bitShiftTypePattern[bitShiftType.ordinal()].matcher(value);
        if (!matcher.find()) return false;
        NumberType firstNumber = NumberType.of(value.substring(0, matcher.start()).trim().replace("_", ""), language);
        if (firstNumber == null) return false;
        NumberType secondNumber = NumberType.of(value.substring(matcher.end()).trim().replace("_", ""), language);
        if (secondNumber == null) return false;
        return true;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "DataFlowIssue"})
    @Override
    public Result getShift(String value, BitShiftType bitShiftType, Language language) {
        Matcher matcher = bitShiftTypePattern[bitShiftType.ordinal()].matcher(value);
        matcher.find();
        String firstValue = value.substring(0, matcher.start()).trim().replace("_", "");
        NumberType firstNumber = NumberType.of(firstValue, language);
        String secondValue = value.substring(matcher.end()).trim().replace("_", "");
        NumberType secondNumber = NumberType.of(secondValue, language);
        return new Result(secondNumber.parse(secondValue, language).intValue(), firstNumber.parse(firstValue, language));
    }

    @Override
    public String orSeperator() {
        return " or ";
    }

    @Override
    public String wrapShift(String expression, int shift, BitShiftType bitShiftType) {
        return expression + " " + bitShiftTypeString[bitShiftType.ordinal()] + " " + shift;
    }

    @Override
    public void wrapOr(StringBuilder left, String right) {
        left.append(" or (").append(right).append(')');
    }
}
