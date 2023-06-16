package com.zelaux.numberconverter.numbertype

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.zelaux.numberconverter.NumberContainer

open class PsiResult private constructor(

    @get:JvmName("context")
    val context: NumberContainer,
    text: String,

    ) {
    private var text_: String? = text

    @get:JvmName("text")
    val text: String
        get() {
            val f = text_;
            if (f == null) {
                val text = psiElement!!.text;
                text_ = text
                return text
            }
            return f
        }

    fun copy(): PsiResult = PsiResult(context, text)
    fun apply(document: Document, container: NumberContainer) {

        val elementOffset = container.element.textOffset
        val beginOffset = container.inElementStart + elementOffset
        val endOffset = container.inElementEnd + elementOffset
        document.replaceString(beginOffset, endOffset, text)
    }

    fun mutateText(newText: String): PsiResult {
        text_ = newText;
        //psiElement=null;
        return this;
    }

    fun mutateElement(element: PsiElement): PsiResult {
        text_ = null;
        this.psiElement=element;
        //psiElement=null;
        return this;
    }

    fun textLength(): Int {
        return text.length
    }

    @get:JvmName("element")
    @set:JvmName("element")
    var psiElement: PsiElement? = null
        private set

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
