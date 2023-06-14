package com.zelaux.numberconverter.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType


fun <T : PsiElement?> PsiElement.findChildByType(type: IElementType): T? {
    @Suppress("UNCHECKED_CAST")
    return node.findChildByType(type)?.psi as T?
}

inline fun <reified T> PsiElement.findChildByClass() = findChildByClass(T::class.java)

@Suppress("UNCHECKED_CAST")
fun <T> PsiElement.findChildByClass(aClass: Class<T>): T? {
    var cur: PsiElement? = firstChild
    while (cur != null) {
        if (aClass.isInstance(cur)) return cur as T
        cur = cur.nextSibling
    }
    return null
}