package com.zelaux.numberconverter.extensionpoints;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.util.xml.Required;
import com.intellij.util.xmlb.annotations.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Property(style = Property.Style.ATTRIBUTE)
public class PopupChoiceBean {
    public static ExtensionPointName<PopupChoiceBean> EP_NAME = ExtensionPointName.create("com.zelaux.numberconverter.actionGroupPopup");
    @Nullable
    public String text;

    public PopupChoiceBean() {
        int i = 0;
    }


    @Nullable
    public String description;
    @Nullable
    public String id;
    @Required
    public String actionGroup;
    final ArrayList<ShortcutDescriptor> shortcuts = new ArrayList<>();

    @Property(surroundWithTag = false)
    @XCollection()
    public void setKeyboardShortcut(ShortcutDescriptor[] descriptors) {
        shortcuts.addAll(Arrays.asList(descriptors));
    }

    public ShortcutDescriptor[] getKeyboardShortcut() {
        return new ShortcutDescriptor[0];
    }

    public ArrayList<ShortcutDescriptor> shortcuts() {
        return shortcuts;
    }
    @Tag("keyboard-shortcut")
    @Property(style=Property.Style.ATTRIBUTE)
    public static class ShortcutDescriptor {
        @Required
        public String keymap;
        @Required
        @Attribute("first-keystroke")
        public String firstKeystroke;
        @Attribute("second-keystroke")
        @Nullable
        public String secondKeystroke;

        public void apply(String actionId) {
            KeyStroke firstKeyStroke = ActionManagerImpl.getKeyStroke(firstKeystroke);
            KeyStroke secondKeyStroke = null;
            if (secondKeystroke != null) secondKeyStroke = ActionManagerImpl.getKeyStroke(secondKeystroke);

            KeymapManagerEx keymapManagerEx = KeymapManagerEx.getInstanceEx();
            KeyboardShortcut shortcut = new KeyboardShortcut(firstKeyStroke, secondKeyStroke);
            keymapManagerEx.getKeymap(keymap).addShortcut(actionId, shortcut);
        }
    }
}
