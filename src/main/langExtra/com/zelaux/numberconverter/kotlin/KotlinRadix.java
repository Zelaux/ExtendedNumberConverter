package com.zelaux.numberconverter.kotlin;

import com.intellij.psi.PsiElement;
import com.intellij.util.IntPair;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.extensions.radix.NoOctalRadix;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.numbertype.PsiResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.KtNodeType;
import org.jetbrains.kotlin.KtNodeTypes;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.KtConstantExpression;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import org.jetbrains.kotlin.psi.KtPsiFactoryKt;

import java.math.BigInteger;

public class KotlinRadix extends NoOctalRadix {
    public static final KotlinRadixNumberType decimal = new KotlinRadixNumberType(DefaultRadixNumberType.Decimal);
    public static final KotlinRadixNumberType hexadecimal = new KotlinRadixNumberType(DefaultRadixNumberType.Hexadecimal);
    public static final KotlinRadixNumberType binary = new KotlinRadixNumberType(DefaultRadixNumberType.Binary);

    public static class KotlinRadixNumberType implements NumberType.RadixType {
        public final DefaultRadixNumberType parentType;

        public KotlinRadixNumberType(DefaultRadixNumberType parentType) {
            this.parentType = parentType;
        }

        @Override
        public String title() {
            return parentType.title();
        }

        @Override
        public boolean match(NumberContainer container, int inElementStart, int inElementEnd) {
            PsiElement element = KotlinCommonPsiResolver.getInstance()
                    .realCommon(container.element, inElementStart, inElementEnd);


            if (element == null) return false;
            if (element.getNode().getElementType() != KtTokens.INTEGER_LITERAL) {
                return false;
            }
            return parentType.pattern.matcher(element.getText()).matches();
        }

        @Override
        public BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd) {
            PsiElement element = KotlinCommonPsiResolver.getInstance()
                    .realCommon(container.element, inElementStart, inElementEnd);

            return new BigInteger(element.getText().substring(parentType.prefix.length()).replace("_", ""), parentType.radix);
        }

        @Override
        public PsiResult wrap(NumberContainer container, BigInteger integer) {
//            KtPsiFactory factory = KtPsiFactoryKt.KtPsiFactory(container.element.getProject());
//            factory.createExpression()
            return parentType.wrap(container, integer);
        }

        @Override
        public @Nullable IntPair numberContentRange(NumberContainer container, int inElementStart, int inElementEnd) {
            PsiElement element = KotlinCommonPsiResolver.getInstance()
                    .realCommon(container.element, inElementStart, inElementEnd);
            int offset = container.element.getTextOffset() - element.getTextOffset();
            return new IntPair(offset + parentType.prefix.length(), offset + element.getTextLength());
        }

        @Override
        public String wrapUnderScore(String numberWithUnderScore) {
            return parentType.wrapUnderScore(numberWithUnderScore);
        }
    }

    @Override
    public @Nullable NumberType.RadixType hexadecimal() {
        return hexadecimal;
    }

    @Override
    public @NotNull NumberType.RadixType decimal() {
        return decimal;
    }

    @Override
    public @Nullable NumberType.RadixType binary() {
        return binary;
    }
}
