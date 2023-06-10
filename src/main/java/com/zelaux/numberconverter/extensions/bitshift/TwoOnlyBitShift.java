package com.zelaux.numberconverter.extensions.bitshift;

import com.intellij.lang.Language;

public class TwoOnlyBitShift extends DefaultBitShift {
    @Override
    public boolean match(String value, BitShiftType bitShiftType, Language language) {
        if (bitShiftType == BitShiftType.unsignedRightShift) return false;
        return super.match(value, bitShiftType, language);
    }
}
