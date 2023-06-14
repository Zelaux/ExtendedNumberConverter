package com.zelaux.numberconverter;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.numbertype.PsiResult;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.stream.Stream;

public final class NumberContainer {
    //	boolean negative;
    public final PsiElement element;
    public final int inElementStart;
    public final int inElementEnd;
    private String elementText = null;

    public String getText(int inElementStart, int inElementEnd) {
        if (elementText == null) elementText = element.getText();
        return elementText.substring(inElementStart, inElementEnd);
    }

    public final Language language;
    private NumberType currentSystem;

    public NumberType currentSystem() {
        return currentSystem;
    }
    //	BigInteger integer;

    private NumberContainer(PsiElement element, int inElementStart, int inElementEnd, Language language) {
        this.element = element;
        this.inElementStart = inElementStart;
        this.inElementEnd = inElementEnd;
        this.language = language == null ? Language.ANY : language;
    }

    @Nullable
    public static NumberContainer createOrNull(PsiElement element, int inElementStart, int inElementEnd, Language language) {
        NumberContainer container = new NumberContainer(element, inElementStart, inElementEnd, language);
        container.currentSystem = NumberType.of(container, inElementStart, inElementEnd);
        if (container.currentSystem == null) return null;
        return container;
    }
    @Nullable
    public static NumberContainer createOrNull(PsiElement element, int inElementStart, int inElementEnd, Language language, Stream<NumberType> numberTypeStream) {
        NumberContainer container = new NumberContainer(element, inElementStart, inElementEnd, language);
        container.currentSystem = NumberType.of(container, inElementStart, inElementEnd,numberTypeStream);
        if (container.currentSystem == null) return null;
        return container;
    }

    public static NumberContainer unsafeCreationUnsafeCreationUnsafeCreation(PsiElement element,
                                                                             int inElementStart,
                                                                             int inElementEnd,
                                                                             Language language,
                                                                             NumberType currentSystem) {
        NumberContainer container = new NumberContainer(element, inElementStart, inElementEnd, language);

        container.currentSystem = currentSystem;
        return container;
    }

    public static NumberContainer create(PsiElement element, int inElementStart, int inElementEnd, Language language) {
        NumberContainer container = createOrNull(element, inElementStart, inElementEnd, language);
        if (container == null) throw new NumberFormatException();
        return container;
    }

    public PsiResult transformPsiElement(NumberType targetSystem) {
//        if (targetSystem == currentSystem) return element;
        return targetSystem.wrap(this, parse());
    }

    public PsiResult psiFromText(String expression) {
        return PsiResult.of(this, expression);
//        ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
//        Project project = element.getProject();
//        if (true) return element;
//        Lexer lexer = parserDefinition.createLexer(project);
//        lexer.start();
//        parserDefinition.createParser(project)
//        parserDefinition.createLexer(project)
//        return PsiElementFactory.getInstance(project)
//                .createExpressionFromText(expression, element.getContext()
//                );
    }

    public BigInteger parse() {
        return currentSystem.parse(this, inElementStart, inElementEnd);
    }
}
