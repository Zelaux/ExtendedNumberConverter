package com.zelaux.numberconverter.extensions.bitshift;

import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.numbertype.PsiResult;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultBitShift extends BitShiftNumberTypeProvider {
    protected final BitShiftTypeExtra[] bitShiftTypeExtra;


    public DefaultBitShift(BitShiftTypeExtra[] bitShiftTypeExtra) {
        this.bitShiftTypeExtra = bitShiftTypeExtra;
    }

    public DefaultBitShift() {
        this(makeExtra("<<", ">>", ">>>"));
    }

    protected static class BitShiftTypeExtra {
        public String literal;
        public Pattern pattern;

        public BitShiftTypeExtra() {
        }

        public BitShiftTypeExtra(String literal, Pattern pattern) {
            this.literal = literal;
            this.pattern = pattern;
        }
    }

    protected static BitShiftTypeExtra[] makeExtra(String leftShift, String rightShift, String unsignedRightShift) {
        BitShiftType[] values = BitShiftType.values();
        BitShiftTypeExtra[] extras = new BitShiftTypeExtra[values.length];
        for (int i = 0; i < values.length; i++) {
            extras[i] = new BitShiftTypeExtra();
            switch (values[i]) {

                case leftShift:
                    extras[i].literal = leftShift;
                    break;
                case rightShift:
                    extras[i].literal = rightShift;
                    break;
                case unsignedRightShift:
                    extras[i].literal = unsignedRightShift;
                    break;
            }
            extras[i].pattern = Pattern.compile("\\s*(" + extras[i].literal + ")\\s*");
        }
        return extras;
    }

    @Override
    public boolean matchShift(NumberContainer container, int inElementStart, int inElementEnd, BitShiftType bitShiftType) {
        Matcher matcher = bitShiftTypeExtra[bitShiftType.ordinal()].pattern.matcher(
                container.getText(inElementStart, inElementEnd)
        );
        if (!matcher.find()) return false;
        NumberType firstNumber = NumberType.of(container,inElementStart,inElementStart+matcher.start()+1);
        if (firstNumber == null) return false;
        NumberType secondNumber = NumberType.of(container,inElementStart+matcher.end(),inElementEnd);
        return secondNumber != null;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "DataFlowIssue"})
    @Override
    public Result getShift(NumberContainer container, int inElementStart, int inElementEnd, BitShiftType bitShiftType) {
        Matcher matcher = bitShiftTypeExtra[bitShiftType.ordinal()].pattern.matcher(
                container.getText(inElementStart, inElementEnd)
        );
        matcher.find();
        BigInteger firstNumber = NumberType.parseStatic(container,inElementStart,inElementStart+matcher.start()+1);
        BigInteger secondNumber = NumberType.parseStatic(container,inElementStart+matcher.end(),inElementEnd);
        return new Result(secondNumber.intValue(), firstNumber);
    }

    @Override
    public PsiResult wrapShift(NumberContainer container, PsiResult expression, int shift, BitShiftType bitShiftType) {

        return container.psiFromText(expression.text() + " " + bitShiftTypeExtra[bitShiftType.ordinal()].literal + " " + shift);
    }

    @Override
    public String orSeperator() {
        return "|";
    }

    @Override
    public PsiResult wrapOr(NumberContainer container, PsiResult left, PsiResult right) {
//        left.append(" ").append(orSeperator()).append(" ").append(right);
        return left.mutateText(left.text()+" "+orSeperator()+" "+right.text());
    }
}
