package com.zelaux.numberconverter.settings;

import java.util.function.Function;

public enum OutputCase {
    UpperCase(String::toUpperCase),
    LowerCase(String::toLowerCase),
    NoTranfsorm(it->it),

    ;
    public final Function<String,String> function;

    OutputCase(Function<String, String> function) {
        this.function = function;
    }

    public String process(String string) {
        return function.apply(string);
    }
}
