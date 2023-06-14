package com.zelaux.numberconverter.extensions.bitshift

import com.zelaux.numberconverter.NumberContainer
import com.zelaux.numberconverter.numbertype.NumberType
import com.zelaux.numberconverter.numbertype.PsiResult
import com.zelaux.numberconverter.utils.PsiUtil
import com.zelaux.numberconverter.utils.findChildByClass
import com.zelaux.numberconverter.utils.findChildByType
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*
import java.math.BigInteger

class KotlinBitShift : DefaultBitShift(makeExtra("shl", "shr", "ushr")) {
    override fun generateNumberTypes(): Array<NumberType> {
        return arrayOf(
            KotlinOrProductNumberType().also {
                or = it
            },
            left, right, unsignedRight
        )
    }

    override fun orSeperator(): String = "or"

    override fun wrapShift(
        container: NumberContainer,
        expression: PsiResult,
        shift: Int,
        bitShiftType: BitShiftType,
    ): PsiResult {
        val result = super.wrapShift(container, expression, shift, bitShiftType)
        if (result.text.matches(Regex("\\([^()]\\)"))) return result
        result.mutateText('('.toString() + result.text + ')')
        return result
    }

    override fun wrapOr(container: NumberContainer, left: PsiResult, right: PsiResult): PsiResult {
        if (right.text.matches(Regex("\\([^()]\\)"))) {
            return left.mutateText(left.text + " or " + right.text)
        } else {
            return left.mutateText(left.text + " or (" + right.text + ")")
        }
    }

    override fun getShift(
        container: NumberContainer,
        inElementStart: Int,
        inElementEnd: Int,
        bitShiftType: BitShiftType,
    ): Result {
        val element = PsiUtil.getCommonPsi(
            container.element,
            container.language,
            inElementStart,
            inElementEnd
        ).let { it as KtExpression }.unwrapBrackets()!!.parse()
        val left = element.first;
        val right = element.second;
        return Result(right.toInt(), left)
    }

    override fun matchShift(
        container: NumberContainer,
        inElementStart: Int,
        inElementEnd: Int,
        bitShiftType: BitShiftType,
    ): Boolean {
        return matchOperator(container, inElementStart, inElementEnd, bitShiftTypeExtra[bitShiftType.ordinal].literal)
    }


    protected inner class KotlinOrProductNumberType : OrProductNumberType() {
        override fun match(container: NumberContainer, inElementStart: Int, inElementEnd: Int): Boolean {
            return matchOperator(container, inElementStart, inElementEnd, "or")
        }


        override fun parse(container: NumberContainer, inElementStart: Int, inElementEnd: Int): BigInteger {
            return PsiUtil.getCommonPsi(
                container.element,
                container.language,
                inElementStart,
                inElementEnd
            ).let { it as KtExpression }.unwrapBrackets()!!.parse().let { it.first.or(it.second) }

        }

        override fun wrap(container: NumberContainer, integer: BigInteger): PsiResult {
            return super.wrap(container, integer)
        }
    }

    private fun matchOperator(
        container: NumberContainer,
        inElementStart: Int,
        inElementEnd: Int,
        operatorWord: String,
    ): Boolean {
        return PsiUtil.getCommonPsi(
            container.element,
            container.language,
            inElementStart,
            inElementEnd
        )?.let { it as? KtExpression }?.unwrapBrackets()?.matches(operatorWord) ?: false
    }

    fun KtExpression.toContainer(): NumberContainer? {
        return NumberContainer.createOrNull(unwrapBrackets(), 0, textLength, KotlinLanguage.INSTANCE)
    }

    fun KtExpression.unwrapBrackets(): KtExpression? {
        if (this is KtParenthesizedExpression) {
            return this.expression?.unwrapBrackets()
        }
        return this;
    }

    private fun KtExpression.matches(operatorWord: String): Boolean {

        if (this is KtBinaryExpression) {
            val operationReferenceIdentifier = try {
                this.operationReference
            } catch (npe: NullPointerException) {
                null
            }?.getIdentifier() ?: return false

            /*if (operationReferenceIdentifier != KtTokens.IDENTIFIER) {
                    return false
                }*/
            if (operationReferenceIdentifier.text != operatorWord) return false

            this.left?.toContainer() ?: return false;
            this.right?.toContainer() ?: return false;
            return true
        } else if (this is KtDotQualifiedExpression) {


            val callExpression = findChildByType<KtCallExpression>(KtNodeTypes.CALL_EXPRESSION) ?: return false
            val referenceExpression = callExpression
                .findChildByType<KtNameReferenceExpression>(KtNodeTypes.REFERENCE_EXPRESSION)
                ?.getIdentifier()?.text
            if(referenceExpression != operatorWord)return false;
            val type = callExpression
                .findChildByType<KtValueArgumentList>(KtNodeTypes.VALUE_ARGUMENT_LIST)
                ?.arguments?.get(0)
                ?.findChildByClass<KtExpression>()
                ?.toContainer() ?: return false
            val receiver = receiverExpression.toContainer() ?: return false;
            return true
        }
        return false;
    }

    private fun KtExpression.parse(): Pair<BigInteger, BigInteger> {
        if (this is KtBinaryExpression) {
            return left?.toContainer()!!.parse() to right?.toContainer()!!.parse()
        } else {
            this as KtDotQualifiedExpression
            val receiver = receiverExpression.toContainer()!!
            val type = findChildByType<KtCallExpression>(KtNodeTypes.CALL_EXPRESSION)!!
                .findChildByType<KtValueArgumentList>(KtNodeTypes.VALUE_ARGUMENT_LIST)!!
                .arguments[0]!!
                .findChildByClass<KtExpression>()!!
                .toContainer()!!
            /*val referenceExpression = type
                .findChildByType<KtNameReferenceExpression>(KtNodeTypes.REFERENCE_EXPRESSION)!!
                .getIdentifier()!!.text*/
            return receiver.parse() to type.parse()
        }
    }
}
