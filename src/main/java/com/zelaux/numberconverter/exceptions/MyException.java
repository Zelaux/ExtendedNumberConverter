package com.zelaux.numberconverter.exceptions;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.zelaux.numberconverter.utils.IdeUtils;
import org.jetbrains.annotations.Nullable;

public class MyException extends RuntimeException{
    @Nullable
    public final PsiElement element;
    public final int inElementStart;
    public final int inElementEnd;
    public final Language language;
    public final Throwable throwable;

    public MyException(@Nullable PsiElement element, int inElementStart, int inElementEnd, Language language, Throwable throwable) {
        this.element = element;
        this.inElementStart = inElementStart;
        this.inElementEnd = inElementEnd;
        this.language = language;
        this.throwable = throwable;
    }
}
