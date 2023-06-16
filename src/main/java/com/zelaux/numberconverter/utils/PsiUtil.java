package com.zelaux.numberconverter.utils;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.zelaux.numberconverter.extensionpoints.CommonPsiResolver;
import org.jetbrains.annotations.Nullable;

public class PsiUtil {
private static boolean inExtention;
    @Nullable
    public static PsiElement getCommonPsi(PsiElement psiElement, Language language, int selectionStart, int selectionEnd) {
        if(!inExtention) {
            try {
                inExtention=true;
                CommonPsiResolver resolver = CommonPsiResolver.LANG_EP.forLanguage(language);
                if(resolver!=null)return resolver.getCommonPsi(psiElement, language, selectionStart, selectionEnd);
            } finally {
                inExtention=false;
            }
        }
        PsiElement element;
        PsiElement beginElement = psiElement.findElementAt(selectionStart);
        PsiElement endElement = psiElement.findElementAt(selectionEnd);
        ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
        TokenSet whitespaceTokens = parserDefinition.getWhitespaceTokens();
        if (beginElement == null && endElement == null) {
            return null;
        } else if (beginElement == null) {
            return endElement;
        } else if (endElement == null) {
            return beginElement;
        }

        while (whitespaceTokens.contains(beginElement.getNode().getElementType()) && selectionStart <= selectionEnd) {
            beginElement = psiElement.findElementAt(++selectionStart);
        }
        while (whitespaceTokens.contains(endElement.getNode().getElementType()) && selectionStart <= selectionEnd) {
            endElement = psiElement.findElementAt(--selectionEnd);
        }
        if (whitespaceTokens.contains(beginElement.getNode().getElementType()) && whitespaceTokens.contains(endElement.getNode().getElementType())) {
            return null;
        }
        if (beginElement.getTextOffset() + beginElement.getTextLength() == selectionEnd) {
            selectionStart = beginElement.getTextOffset();
            element = beginElement;
        } else {
            element = PsiTreeUtil.findCommonParent(beginElement, endElement);
        }

        return element;
    }

    @Nullable
    public static CommonPsiAndRanges getCommonPsiAndRanges(PsiElement psiElement, Language language, int selectionStart, int selectionEnd) {
        if(!inExtention) {
            try {
                inExtention=true;
                CommonPsiResolver resolver = CommonPsiResolver.LANG_EP.forLanguage(language);
                if(resolver!=null)return resolver.getCommonPsiAndRanges(psiElement, language, selectionStart, selectionEnd);
            } finally {
                inExtention=false;
            }
        }
        ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
        TokenSet commentTokens = parserDefinition.getCommentTokens();
        TokenSet stringLiterals = parserDefinition.getStringLiteralElements();

        inExtention=!inExtention;
        PsiElement element;
        try {
            element = getCommonPsi(psiElement, language, selectionStart, selectionEnd);
        } finally {
            inExtention=!inExtention;
        }

        if (element == null) return null;
        if (commentTokens.contains(element.getNode().getElementType()) || stringLiterals.contains(element.getNode().getElementType())) {
            int offset = element.getTextOffset();
            return new CommonPsiAndRanges(
                    element,
                    selectionStart - offset,
                    selectionEnd - offset
            );
        } else {
            return new CommonPsiAndRanges(
                    element,
                    0,
                    element.getTextLength()
            );
        }
    }

    public static final class CommonPsiAndRanges {
        public final PsiElement element;
        public final int inElementStart;
        public final int inElementEnd;

        public CommonPsiAndRanges(PsiElement element, int inElementStart, int inElementEnd) {
            this.element = element;
            this.inElementStart = inElementStart;
            this.inElementEnd = inElementEnd;
        }
    }
}
