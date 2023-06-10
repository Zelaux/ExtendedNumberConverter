package com.zelaux.numberconverter;

import com.intellij.lang.Language;
import com.zelaux.numberconverter.numbertype.NumberType;

public final class ParsedNumber {
    //	boolean negative;
    final String value;
    final NumberType currentSystem;
//	BigInteger integer;

    public ParsedNumber(String value,Language language) {
        this.value = value.strip().replaceAll("_", "");
        currentSystem = NumberType.of(this.value, language);
        if(currentSystem==null)throw new NumberFormatException();
    }

    public String toString(NumberType system, Language language) {
        if (system == currentSystem) return value;
        return system.wrap(currentSystem.parse(value, language), language);
    }
}
