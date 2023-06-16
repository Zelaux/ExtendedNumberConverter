package com.zelaux.numberconverter.extensions.highlight;

import com.intellij.icons.AllIcons;
import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.tree.IElementType;
import com.zelaux.numberconverter.Vars;
import com.zelaux.numberconverter.highlight.MyHighlightManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class MyColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Selected number expression", MyHighlightManager.SELECTED_NUMBER_EXPRESSION)
    };

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Icons.Ide.MenuArrow;
    }
    /*
    @Nullable
    @Override
    public Icon getIcon() {
        return SimpleIcons.FILE;
    }*/

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new DemoSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "some text 111000 ff";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return Vars.pluginName;
    }

    public static class DemoSyntaxHighlighter implements SyntaxHighlighter {

        @Override
        public @NotNull Lexer getHighlightingLexer() {
            return new JavaLexer(LanguageLevel.JDK_16);
        }

        @Override
        public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
            if (tokenType == JavaTokenType.INTEGER_LITERAL) {
                return new TextAttributesKey[]{MyHighlightManager.SELECTED_NUMBER_EXPRESSION};
            } else {
                return new TextAttributesKey[0];
            }
        }

    }
}