package com.zelaux.numberconverter.extensions.bitshift

import com.zelaux.numberconverter.NumberContainer
import com.zelaux.numberconverter.numbertype.NumberType
import com.zelaux.numberconverter.numbertype.PsiResult
import com.zelaux.numberconverter.utils.PsiUtil
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
        if (result.text.matches(Regex("\\([^()]\\)")))return result
        result.mutateText('('.toString() + result.text + ')')
        return result
    }

    override fun wrapOr(container: NumberContainer, left: PsiResult, right: PsiResult): PsiResult {
        if(right.text.matches(Regex("\\([^()]\\)"))){
            return left.mutateText(left.text + " or " + right.text)
        } else{
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
        ) as KtBinaryExpression
        val left=element.left?.toContainer()!!.parse();
        val right=element.right?.toContainer()!!.parse();
        return Result(right.toInt(),left)
    }

    override fun matchShift(
        container: NumberContainer,
        inElementStart: Int,
        inElementEnd: Int,
        bitShiftType: BitShiftType,
    ): Boolean {
        return matchOperator(container,inElementStart,inElementEnd,bitShiftTypeExtra[bitShiftType.ordinal].literal)
    }


    protected inner class KotlinOrProductNumberType : OrProductNumberType() {
        override fun match(container: NumberContainer, inElementStart: Int, inElementEnd: Int): Boolean {
            return matchOperator(container, inElementStart, inElementEnd, "or")
        }



        override fun parse(container: NumberContainer, inElementStart: Int, inElementEnd: Int): BigInteger {
            val element = PsiUtil.getCommonPsi(
                container.element,
                container.language,
                inElementStart,
                inElementEnd
            ) as KtBinaryExpression

            return element.left?.toContainer()!!.parse().or(element.right?.toContainer()!!.parse())

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
        val element = PsiUtil.getCommonPsi(
            container.element,
            container.language,
            inElementStart,
            inElementEnd
        ) as? KtBinaryExpression ?: return false
        val operationReferenceIdentifier = try {
            element.operationReference
        } catch (npe: NullPointerException) {
            null
        }?.getIdentifier() ?: return false

        /*if (operationReferenceIdentifier != KtTokens.IDENTIFIER) {
                return false
            }*/
        if (operationReferenceIdentifier.text != operatorWord) return false

        element.left?.toContainer() ?: return false;
        element.right?.toContainer() ?: return false;
        return true
    }
    fun KtExpression.toContainer(first: Boolean = true): NumberContainer? {
        if (first && this is KtParenthesizedExpression) {
            return this.expression?.toContainer()
        }
        return NumberContainer.createOrNull(this, 0,  textLength, KotlinLanguage.INSTANCE)
    }
}
