package com.zelaux.numberconverter.kotlin.numberProviders;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.extensionpoints.NumberTypeProvider;
import com.zelaux.numberconverter.kotlin.KotlinCommonPsiResolver;
import com.zelaux.numberconverter.kotlin.ToolsKt;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.numbertype.PsiResult;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtUnaryExpression;

import java.math.BigInteger;

public class KotlinUnary implements NumberTypeProvider {
    public static final KotlinUnaryNumberType kotlinUnary=new KotlinUnaryNumberType();
    @Override
    public NumberType[] getNumberTypes() {
        return new NumberType[]{kotlinUnary};
    }

    public static final TokenSet availableUnary = TokenSet.create(KtTokens.PLUS, KtTokens.MINUS);

    public static class KotlinUnaryNumberType implements NumberType {

        @Override
        public String title() {
            return "Unary";
        }

        @Override
        public boolean match(NumberContainer container, int inElementStart, int inElementEnd) {
            PsiElement element = KotlinCommonPsiResolver.getInstance().realCommon(
                    container.element,
                    inElementStart,
                    inElementEnd
            );
            if(element instanceof KtExpression){
                element=ToolsKt.unwrapBrackets((KtExpression) element);
            }
            if (!(element instanceof KtExpression) || element == null) return false;

            if (!(element instanceof KtUnaryExpression)) return false;

            KtUnaryExpression unaryExpression = (KtUnaryExpression) element;
            if (!availableUnary.contains(unaryExpression.getOperationToken())) return false;
            NumberContainer numberContainer = ToolsKt.toContainer(unaryExpression.getBaseExpression());
            return numberContainer != null;
        }

        @Override
        public BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd) {
            PsiElement element = KotlinCommonPsiResolver.getInstance().realCommon(
                    container.element,
                    inElementStart,
                    inElementEnd
            );
            KtUnaryExpression expression = (KtUnaryExpression) ToolsKt.unwrapBrackets((KtExpression) element);
            NumberContainer numberContainer = ToolsKt.toContainer(expression.getBaseExpression());
            BigInteger parse = numberContainer.parse();
            if (expression.getOperationToken() == KtTokens.MINUS) {
                return parse.negate();
            }
            return parse;
        }

        @Override
        public PsiResult wrap(NumberContainer container, BigInteger integer) {
            return null;
        }
    }
}
