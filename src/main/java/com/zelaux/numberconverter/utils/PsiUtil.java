package com.zelaux.numberconverter.utils;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public class PsiUtil {

    @Nullable
    public static PsiElement getCommonPsi(PsiElement psiElement, Language language, int selectionStart, int selectionEnd) {
        PsiElement element;
        PsiElement beginElement = psiElement.findElementAt(selectionStart);
        PsiElement endElement = psiElement.findElementAt(selectionEnd);
        ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
        TokenSet whitespaceTokens = parserDefinition.getWhitespaceTokens();
        if (beginElement == null && endElement == null) {
            return null;
        } else if (beginElement == null) {
            element = endElement;
        } else if (endElement == null) {
            element = beginElement;
        } else {
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
                selectionStart =beginElement.getTextOffset();
                element = beginElement;
            } else {
                element = PsiTreeUtil.findCommonParent(beginElement, endElement);
            }
        }
        return element;
    }
}
