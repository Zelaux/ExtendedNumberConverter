package com.zelaux.numberconverter.kotlin;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.zelaux.numberconverter.extensionpoints.CommonPsiResolver;
import com.zelaux.numberconverter.utils.PsiUtil;
import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.parsing.KotlinParserDefinition;
import org.jetbrains.kotlin.psi.*;

import java.util.HashSet;

public class KotlinCommonPsiResolver implements CommonPsiResolver {
    private static final TokenSet skippedOperations = TokenSet.create(
            KtTokens.EQ,
            KtTokens.MULTEQ,
            KtTokens.DIVEQ,
            KtTokens.PLUSEQ,
            KtTokens.MINUSEQ,
            KtTokens.PERCEQ
    );

    public static KotlinCommonPsiResolver getInstance() {
        return (KotlinCommonPsiResolver) CommonPsiResolver.LANG_EP.forLanguage(KotlinLanguage.INSTANCE);
    }

    private static final TokenSet whitespaceTokens = KotlinParserDefinition.Companion.getInstance().getWhitespaceTokens();
    private static final TokenSet commentTokens = KotlinParserDefinition.Companion.getInstance().getCommentTokens();
    private static final TokenSet stringLiterals = KotlinParserDefinition.Companion.getInstance().getStringLiteralElements();
    private static final HashSet<String> goUpIdentifiers = new HashSet<>(CollectionsKt.listOf(
            "shl", "shr", "ushl", "or", "and"
    ));

    @Override
    @Nullable
    public PsiElement getCommonPsi(PsiElement psiElement, Language language, int selectionStart, int selectionEnd) {
        PsiElement element = getRawPsiElement(psiElement, selectionStart, selectionEnd);
        return postProcess(element);
    }

    @Nullable
    private static PsiElement postProcess(PsiElement element) {
        if (element == null) return null;
        PsiElement parent = element.getParent();
        if(!noReversion){
            if (element instanceof KtDotQualifiedExpression && parent instanceof KtDotQualifiedExpression) {
                return postProcess(parent);
            }
            if (element instanceof KtBinaryExpression && parent instanceof KtBinaryExpression) {
                return postProcess(parent);
            }
            if (element instanceof KtParenthesizedExpression && parent instanceof KtExpression && !(parent instanceof KtDeclaration)) {
                return postProcess(parent);
            }
        }
        if (element.getNode().getElementType() != KtTokens.IDENTIFIER) return element;
        if (!goUpIdentifiers.contains(element.getText())) return element;
        if (parent instanceof KtNameReferenceExpression) {
            PsiElement parent1 = parent.getParent();
            if (parent1 instanceof KtCallExpression) {
                if (parent1.getParent() instanceof KtDotQualifiedExpression) {
                    return postProcess(parent1.getParent());
                }
            }
        } else if (parent instanceof KtOperationReferenceExpression) {
            return postProcess(parent.getParent());
        }
        return element;
    }

    @Nullable
    private static PsiElement getRawPsiElement(PsiElement psiElement, int selectionStart, int selectionEnd) {
        PsiElement beginElement = psiElement.findElementAt(selectionStart);
        PsiElement endElement = psiElement.findElementAt(selectionEnd);
        if (beginElement == null && endElement == null) {
            return null;
        } else if (beginElement == null) {
            return endElement;
        } else if (endElement == null) {
            return beginElement;
        }

        while (selectionStart < selectionEnd) {
            IElementType elementType = beginElement.getNode().getElementType();
            if (!whitespaceTokens.contains(elementType) && !skippedOperations.contains(elementType)) break;
            beginElement = psiElement.findElementAt(++selectionStart);
        }
        while (selectionStart < selectionEnd) {
            IElementType elementType = beginElement.getNode().getElementType();
            if (!whitespaceTokens.contains(elementType) && elementType != KtTokens.SEMICOLON) break;
            endElement = psiElement.findElementAt(--selectionEnd);
        }
        if (whitespaceTokens.contains(beginElement.getNode().getElementType()) && whitespaceTokens.contains(endElement.getNode().getElementType())) {
            return null;
        }
        if (beginElement.getTextOffset() + beginElement.getTextLength() == selectionEnd) {
            selectionStart = beginElement.getTextOffset();
            return beginElement;
        } else {
            IElementType beginType = beginElement.getNode().getElementType();
            if (selectionStart == selectionEnd && (beginType == KtTokens.RPAR || beginType==KtTokens.SEMICOLON)) {
                PsiElement found = psiElement.findElementAt(selectionStart - 1);
                if (found != null) return found;
            } else if (
                    beginType == KtTokens.LPAR ||
                            beginType == KtTokens.DOT
            ) {
                beginElement = beginElement.getParent();
            }
            return PsiTreeUtil.findCommonParent(beginElement, endElement);
        }
    }

    @Nullable
    @Override
    public PsiUtil.CommonPsiAndRanges getCommonPsiAndRanges(PsiElement psiElement, Language language, int selectionStart, int selectionEnd) {
        return PsiUtil.getCommonPsiAndRanges(psiElement, language, selectionStart, selectionEnd);
//        PsiElement element = getCommonPsi(psiElement, language, selectionStart, selectionEnd);
//
//
//        if (element == null) return null;
//        if (commentTokens.contains(element.getNode().getElementType()) || stringLiterals.contains(element.getNode().getElementType())) {
//            int offset = element.getTextOffset();
//            return new PsiUtil.CommonPsiAndRanges(
//                    element,
//                    selectionStart - offset,
//                    selectionEnd - offset
//            );
//        } else {
//            return new PsiUtil.CommonPsiAndRanges(
//                    element,
//                    0,
//                    element.getTextLength()
//            );
//        }
    }

    private static boolean noReversion;

    @Nullable
    public PsiElement realCommon(PsiElement element, int inElementStart, int inElementEnd) {
        noReversion = true;
        try{
         return getCommonPsi(element, KotlinLanguage.INSTANCE, inElementStart, inElementEnd);
        }finally {
            noReversion=false;
        }
    }
}
