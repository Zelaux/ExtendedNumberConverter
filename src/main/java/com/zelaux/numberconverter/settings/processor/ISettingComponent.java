package com.zelaux.numberconverter.settings.processor;

import com.zelaux.numberconverter.settings.MySettingsState;

import javax.swing.*;

public interface ISettingComponent {
    void applyOn(Object object);
    void reset(Object object);

    JComponent getPanel();

    boolean isModified(Object object);
}
