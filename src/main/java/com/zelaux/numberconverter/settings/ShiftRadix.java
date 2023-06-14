package com.zelaux.numberconverter.settings;

import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;

public abstract class ShiftRadix {
    public final DefaultRadixNumberType delegate;

    private ShiftRadix(DefaultRadixNumberType delegate) {
        this.delegate = delegate;
    }

    public static ShiftRadix of(DefaultRadixNumberType defaultRadixNumberType) {
        return all[defaultRadixNumberType.ordinal()];
    }

    @Override
    public String toString() {
        return delegate.name();
    }

    public final static ShiftRadix[] all;

    static {
        DefaultRadixNumberType[] values = DefaultRadixNumberType.values();
        all = new ShiftRadix[values.length];
        for (int i = 0; i < values.length; i++) {
            all[i] = new ShiftRadix(values[i]) {
            };
        }
    }
}
