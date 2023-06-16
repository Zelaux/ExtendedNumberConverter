package com.zelaux.numberconverter.extensionpoints;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.psi.PsiElement;
import com.zelaux.numberconverter.utils.PsiUtil;
import org.jetbrains.annotations.Nullable;

public interface CommonPsiResolver {

//    ExtensionPointName<LanguageExtensionPoint> EP_NAME = ExtensionPointName.create("com.zelaux.numberconverter.commonPsiResolver");
    LanguageExtension<CommonPsiResolver> LANG_EP = new LanguageExtension<>("NumberManipulation.commonPsiResolver");

    @Nullable
     PsiElement getCommonPsi(PsiElement psiElement, Language language, int selectionStart, int selectionEnd);
    @Nullable
    PsiUtil.CommonPsiAndRanges getCommonPsiAndRanges(PsiElement psiElement, Language language, int selectionStart, int selectionEnd);

}
