package com.zelaux.numberconverter.numbertype

import com.intellij.psi.PsiElement
import com.zelaux.numberconverter.NumberContainer

class PsiResult private constructor(

    @get:JvmName("context")
    val context: NumberContainer,
    text: String,

    ) {
    @get:JvmName("text")
    var text = text
        private set

    fun copy(): PsiResult = PsiResult(context, text)
    fun mutateText(newText: String): PsiResult {
        text = newText;
        //psiElement=null;
        return this;
    }

    fun textLength(): Int {
        return text.length
    }

    /*@get:JvmName("element")
    @set:JvmName("element")
    var psiElement:PsiElement?=null
        private set*/
    companion object {
        /*@JvmStatic
        fun of(context: NumberContainer,psiElement: PsiElement)=PsiResult(context,psiElement.text).also{
            it.psiElement=psiElement
        }*/
        @JvmStatic
        fun of(context: NumberContainer, text: String) = PsiResult(context, text).also {

        }

    }
}
