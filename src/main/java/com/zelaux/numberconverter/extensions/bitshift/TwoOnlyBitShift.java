package com.zelaux.numberconverter.extensions.bitshift;

import com.zelaux.numberconverter.NumberContainer;

public class TwoOnlyBitShift extends DefaultBitShift {


    @Override
    public boolean matchShift(NumberContainer container, int inElementStart, int inElementEnd, BitShiftType bitShiftType) {
        if (bitShiftType == BitShiftType.unsignedRightShift) return false;
        return super.matchShift(container, inElementStart, inElementEnd, bitShiftType);
    }
}
