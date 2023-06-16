package com.zelaux.numberconverter.extensions.bitshift;

import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.extensionpoints.NumberTypeProvider;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.numbertype.PsiResult;
import com.zelaux.numberconverter.settings.MySettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

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
        public String title() {
            return "OrProduct";
        }

        @Override
        public boolean match(NumberContainer container, int inElementStart, int inElementEnd) {
            String value = container.getText(inElementStart, inElementEnd);
            int index = value.indexOf(orSeperator());
            if (index == -1) return false;
            NumberType left = NumberType.of(container, inElementStart, inElementStart + index);
            if (left == null) return false;
            NumberType right = NumberType.of(container, inElementStart + index + 1, inElementEnd);
            if (right == null) return false;
            return true;
        }

        @Override
        public BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd) {
            String value = container.getText(inElementStart, inElementEnd);
            int index = value.indexOf(orSeperator());
            NumberType left = NumberType.of(container, inElementStart, inElementStart + index);
            NumberType right = NumberType.of(container, inElementStart + index + 1, inElementEnd);
            return left.parse(container, inElementStart, inElementStart + index)
                    .or(
                            right.parse(container, inElementStart + index + 1, inElementEnd)
                    );
        }

        @Override
        public PsiResult wrap(NumberContainer container, BigInteger integer) {
            if (integer.equals(BigInteger.ZERO)) return container.psiFromText("0");
            int length = integer.bitLength();
            int offset = integer.getLowestSetBit();
            PsiResult ONE = container.psiFromText("1");
            PsiResult begin = wrapShift(container, ONE, offset, BitShiftType.leftShift);
            for (int i = offset + 1; i <= length; i++) {
                if (integer.testBit(i)) {
                    PsiResult wrapped = wrapShift(container, ONE.copy(), i, BitShiftType.leftShift);
                    begin = wrapOr(container, begin, wrapped);
                }
            }
            return begin;
        }
    }

    ;

    protected BitShiftNumberTypeProvider() {
        if (first) {
            first = false;

        }
    }


    protected final NumberType[] numberTypes = generateNumberTypes();

    @NotNull
    protected NumberType[] generateNumberTypes() {
        return new NumberType[]{
                or,
                left, right, unsignedRight
        };
    }

    @Override
    public NumberType[] getNumberTypes() {
        return numberTypes;
    }

    protected class BitShiftNumberType implements NumberType {
        public final BitShiftType bitShiftType;

        @Override
        public String title() {
            return bitShiftType.name();
        }

        protected BitShiftNumberType(BitShiftType bitShiftType) {
            this.bitShiftType = bitShiftType;
        }

        @Override
        public boolean match(NumberContainer container, int inElementStart, int inElementEnd) {
            return BitShiftNumberTypeProvider.this.matchShift(container, inElementStart, inElementEnd, bitShiftType);
        }

        @Override
        public BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd) {
            Result result = BitShiftNumberTypeProvider.this.getShift(container, inElementStart, inElementEnd, bitShiftType);
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
        public PsiResult wrap(NumberContainer container, BigInteger integer) {
            int offset = integer.getLowestSetBit();
            NumberType mapper = getPreferredType(container);
            PsiResult wrapped = mapper.wrap(container, integer.shiftRight(offset));
            if (offset == 0) return wrapped;
            return BitShiftNumberTypeProvider.this.wrapShift(container, wrapped, offset, BitShiftType.leftShift);
        }

        @NotNull
        private NumberType getPreferredType(NumberContainer container) {
            RadixNumberTypeProvider value = RadixNumberTypeProvider.LANG_EP.forLanguage(container.language);
            DefaultRadixNumberType shiftRadix = MySettingsState.getInstance().shiftRadix.delegate;
            if (value == null) {
                return shiftRadix;
            }
            NumberType preferredType = getPreferredType(value, shiftRadix);
            if (preferredType == null) return value.decimal();
            return preferredType;
        }

        @Nullable
        private NumberType getPreferredType(RadixNumberTypeProvider provider, DefaultRadixNumberType shiftRadix) {
            switch (shiftRadix) {
                case Binary:
                    return provider.binary();
                case Octal:
                    return provider.octal();
                case Decimal:
                    return provider.decimal();
                case Hexadecimal:
                    return provider.hexadecimal();
                default:
                    throw new IllegalStateException("Unexpected value: " + shiftRadix);
            }
        }
    }


    public abstract boolean matchShift(NumberContainer container, int inElementStart, int inElementEnd, BitShiftType bitShiftType);

    public abstract Result getShift(NumberContainer container, int inElementStart, int inElementEnd, BitShiftType bitShiftType);

    public static class Result {
        int shift;
        BigInteger valueToShift;

        public Result(int shift, BigInteger valueToShift) {
            this.shift = shift;
            this.valueToShift = valueToShift;
        }
    }


    public abstract PsiResult wrapShift(NumberContainer container, PsiResult expression, int shift, BitShiftType bitShiftType);

    public abstract String orSeperator();

    public abstract PsiResult wrapOr(NumberContainer container, PsiResult left, PsiResult right);
}
