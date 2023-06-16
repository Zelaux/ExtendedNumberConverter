package com.zelaux.numberconverter.extensionpoints;

import com.intellij.codeInsight.template.postfix.templates.LanguagePostfixTemplate;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NumberTypeProvider {

    ExtensionPointName<LanguageExtensionPoint> EP_NAME = ExtensionPointName.create("NumberManipulation.numberTypeProvider");
    LanguageExtension<NumberTypeProvider> LANG_EP = new LanguageExtension<>(EP_NAME.getName());

    NumberType[] getNumberTypes();

    interface Test{

    }
    ////    ColorExpParserSequence getColorExpressionFrom(@NotNull PsiElement element);

}
