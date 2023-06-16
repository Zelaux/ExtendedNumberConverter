package com.zelaux.numberconverter.kotlin

import com.zelaux.numberconverter.NumberContainer
import com.zelaux.numberconverter.utils.findChildByClass
import com.zelaux.numberconverter.utils.findChildByType
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*
import java.math.BigInteger


fun KtExpression?.toContainer(): NumberContainer? {
    this ?: return null
    val unwrapped = unwrapBrackets() ?: return null
    return NumberContainer.createOrNull(unwrapped, 0, unwrapped.textLength, KotlinLanguage.INSTANCE)
}

fun KtExpression.unwrapBrackets(): KtExpression? {
    if (this is KtParenthesizedExpression) {
        return this.expression?.unwrapBrackets()
    }
    return this;
}

fun KtExpression.matches(operatorWord: String): Boolean {

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
        if (referenceExpression != operatorWord) return false;
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

fun KtExpression.parse(): Pair<BigInteger, BigInteger> {
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