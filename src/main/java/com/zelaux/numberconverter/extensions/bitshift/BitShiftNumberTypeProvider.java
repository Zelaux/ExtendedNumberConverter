package com.zelaux.numberconverter.extensions.bitshift;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.zelaux.numberconverter.extensionpoints.NumberTypeProvider;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;

import java.math.BigInteger;
import java.util.Optional;

public abstract class BitShiftNumberTypeProvider implements NumberTypeProvider {
//    leftShift(LanguageBitShiftProvider.BitShiftType.leftShift),
//    rightShift(LanguageBitShiftProvider.BitShiftType.rightShift),
//    unsignedShiftRight(LanguageBitShiftProvider.BitShiftType.unsignedShiftRight),
    ;

    static boolean first = true;
    public NumberType left = new BitShiftNumberType(BitShiftType.leftShift);
    public NumberType right = new BitShiftNumberType(BitShiftType.rightShift);
    public NumberType unsignedRight = new BitShiftNumberType(BitShiftType.unsignedRightShift);
    public NumberType or = new OrProductNumberType();
    protected class OrProductNumberType implements NumberType {
        @Override
        public String toString() {
            return "or product";
        }

        @Override
        public boolean match(String value, Language language) {
            int index = value.indexOf(orSeperator());
            if (index == -1) return false;
            String leftString = value.substring(0, index);
            NumberType left = NumberType.of(leftString, language);
            if (left == null) return false;
            String rightString = value.substring(index+1);
            NumberType right = NumberType.of(rightString, language);
            if (right == null) return false;
            return true;
        }

        @Override
        public BigInteger parse(String value, Language language) {
            int index = value.indexOf(orSeperator());
            String leftString = value.substring(0, index);
            NumberType left = NumberType.of(leftString, language);
            String rightString = value.substring(index+1);
            NumberType right = NumberType.of(rightString, language);
            return left.parse(leftString, language).or(right.parse(rightString, language));
        }

        @Override
        public String wrap(BigInteger integer, Language language) {
            if (integer.equals(BigInteger.ZERO)) return "0";
            int length = integer.bitLength();
            int offset = integer.getLowestSetBit();
            StringBuilder builder = new StringBuilder((length - offset + 1) * 3);
            for (int i = length; i >= offset; i--) {
                if (integer.testBit(i)) {
                    String wrapped = wrapShift("1", i, BitShiftType.leftShift);
                    if (builder.length() == 0) {
                        builder.append(wrapped);
                    } else {
                        wrapOr(builder, wrapped);
                    }
                }
            }
            return builder.toString();
        }
    };

    protected BitShiftNumberTypeProvider() {
        if (first) {
            first = false;

        }
    }


    @Override
    public AnAction[] actions() {

        return new AnAction[]{
        };
    }

    protected final NumberType[] numberTypes = {
            or,
            left, right, unsignedRight
    };

    @Override
    public NumberType[] getNumberTypes() {
        return numberTypes;
    }

    protected class BitShiftNumberType implements NumberType {
        public final BitShiftType bitShiftType;

        @Override
        public String toString() {
            return bitShiftType.name();
        }

        protected BitShiftNumberType(BitShiftType bitShiftType) {
            this.bitShiftType = bitShiftType;
        }

        @Override
        public boolean match(String value, Language language) {
            return BitShiftNumberTypeProvider.this.match(value, bitShiftType, language);
        }

        @Override
        public BigInteger parse(String value, Language language) {
            Result result = BitShiftNumberTypeProvider.this.getShift(value, bitShiftType, language);
            BigInteger number = result.valueToShift;
            int shift = result.shift;
            switch (bitShiftType) {
                case leftShift:
                    return number.shiftLeft(shift);
                case rightShift:
                    return number.shiftRight(shift);
                case unsignedRightShift:
                    if (number.signum() > 0) return number.shiftRight(shift);
                    StringBuilder stringBuilder = new StringBuilder();
                    int length = number.bitLength();
                    for (int i = 0; i <= length - shift; i++) {
                        if (number.testBit(i)) {
                            stringBuilder.append('1');
                        } else {
                            stringBuilder.append('0');
                        }
                    }

                    return new BigInteger(stringBuilder.toString(), 2);
                default:
                    throw new IllegalStateException("Unexpected value: " + bitShiftType);
            }
        }

        @Override
        public String wrap(BigInteger integer, Language language) {
            int length = integer.bitLength();
            int offset = integer.getLowestSetBit();
            StringBuilder builder = new StringBuilder(length - offset + 1 + 2);
            NumberType mapper = Optional.ofNullable(RadixNumberTypeProvider.LANG_EP.forLanguage(language))
                    .map(it -> {
                        NumberType binary = it.binary();
                        return binary == null ? it.decimal() : binary;
                    }).map(it -> it == DefaultRadixNumberType.binary ? null : it).orElse(null);
            if (mapper == null) builder.append("0b");
            int startLen = builder.length();
            for (int i = length; i >= offset; i--) {
                if (integer.testBit(i)) {
                    builder.append('1');
                } else if (builder.length() > startLen) {
                    builder.append('0');
                }
            }
            if (builder.length() == 0) builder.append("0");
            if (builder.length() == startLen) builder.setLength(1);
            if (mapper != null && builder.length() > 1) {
                BigInteger bigInteger = new BigInteger(builder.toString(), 2);
                builder.setLength(0);
                builder.append(mapper.wrap(bigInteger, language));
            }
            return BitShiftNumberTypeProvider.this.wrapShift(builder.toString(), offset, BitShiftType.leftShift);
        }
    }


    public abstract boolean match(String value, BitShiftType bitShiftType, Language language);

    public abstract Result getShift(String value, BitShiftType bitShiftType, Language language);

    class Result {
        int shift;
        BigInteger valueToShift;

        public Result(int shift, BigInteger valueToShift) {
            this.shift = shift;
            this.valueToShift = valueToShift;
        }
    }


    public abstract String wrapShift(String expression, int shift, BitShiftType bitShiftType);
    public abstract String orSeperator();
    public abstract void wrapOr(StringBuilder left, String right);
}
