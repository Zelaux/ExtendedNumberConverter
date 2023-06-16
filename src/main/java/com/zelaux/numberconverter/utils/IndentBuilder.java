package com.zelaux.numberconverter.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class IndentBuilder extends IndentStream {
    public final ByteArrayOutputStream stream;
    private static ByteArrayOutputStream tmp;

    public IndentBuilder(String indent) {
        super(new PrintStream(tmp = new ByteArrayOutputStream()),indent);
        stream = tmp;
        tmp = null;
    }
    public IndentBuilder(){
        this("\t");
    }

    @Override
    public String toString() {
        return stream.toString();
    }
}
