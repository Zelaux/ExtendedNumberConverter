package com.zelaux.numberconverter.settings.processor;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.fields.IntegerField;
import javaslang.Function1;
import javaslang.Function2;

import javax.script.ScriptEngine;
import javax.swing.*;

public class DefaultDescriptors {
    static void register(ScriptEngine engine) {
        engine.put("intField", (Function2<Integer, Integer, JComponentDescriptor>) (min, max) -> {
            return new JComponentDescriptor(
                    new IntegerField("_", min, max),
                    IntegerField::getValue,
                    (param, val) -> param.setValue((Integer) val));
        });
        engine.put("comboBox", (Function1<Object[], JComponentDescriptor>) (objects) -> {
            return new JComponentDescriptor(new ComboBox<>(objects),
                    JComboBox::getSelectedItem,
                    JComboBox::setSelectedItem);
        });
    }

    public static class IntegerDescriptor {

    }
}
