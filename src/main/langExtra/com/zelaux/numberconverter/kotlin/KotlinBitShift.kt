package com.zelaux.numberconverter.kotlin

import com.zelaux.numberconverter.NumberContainer
import com.zelaux.numberconverter.extensions.bitshift.BitShiftType
import com.zelaux.numberconverter.extensions.bitshift.DefaultBitShift
import com.zelaux.numberconverter.numbertype.NumberType
import com.zelaux.numberconverter.numbertype.PsiResult
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
        if (result.text.matches(Regex("\\([^()]+\\)"))) return result
        result.mutateText('('.toString() + result.text + ')')
        return result
    }

    override fun wrapOr(container: NumberContainer, left: PsiResult, right: PsiResult): PsiResult {
        if (right.text.matches(Regex("\\([^()]+\\)"))) {
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
        val element = KotlinCommonPsiResolver.getInstance().realCommon(
            container.element,
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
            return KotlinCommonPsiResolver.getInstance().realCommon(
                container.element,
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
        return KotlinCommonPsiResolver.getInstance().realCommon(
            container.element,
            inElementStart,
            inElementEnd
        )?.let { it as? KtExpression }?.unwrapBrackets()?.matches(operatorWord) ?: false
    }

}
