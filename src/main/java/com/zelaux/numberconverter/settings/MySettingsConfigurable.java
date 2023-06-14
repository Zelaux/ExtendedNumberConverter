package com.zelaux.numberconverter.settings;

import com.intellij.openapi.options.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class MySettingsConfigurable implements Configurable{

    private MySettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName(){
        return "Number Manipulation";
    }

    @Override
    public JComponent getPreferredFocusedComponent(){
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent(){
        mySettingsComponent = new MySettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified(){
        MySettingsState settings = MySettingsState.getInstance();
        boolean modified = false;
        modified |= mySettingsComponent.getOutputCase() != settings.outputCase;
        modified |= mySettingsComponent.getDecimalSeparator() != settings.decimalSeparator;
        modified |= mySettingsComponent.getBinarySeparator() != settings.binarySeparator;
        modified |= mySettingsComponent.getOctalSeparator() != settings.octalSeparator;
        modified |= mySettingsComponent.getHexSeparator() != settings.hexSeparator;
        modified |= mySettingsComponent.getMaxLen() != settings.maxContentLen;
        return modified;
    }

    @Override
    public void apply(){
        MySettingsState settings = MySettingsState.getInstance();
        settings.outputCase = mySettingsComponent.getOutputCase();

        settings.binarySeparator = mySettingsComponent.getBinarySeparator();
        settings.octalSeparator = mySettingsComponent.getOctalSeparator();
        settings.decimalSeparator = mySettingsComponent.getDecimalSeparator();
        settings.hexSeparator = mySettingsComponent.getHexSeparator();
        settings.maxContentLen = mySettingsComponent.getMaxLen();
    }

    @Override
    public void reset(){
        MySettingsState settings = MySettingsState.getInstance();
        mySettingsComponent.setOutputCase(settings.outputCase);

        mySettingsComponent.setBinarySeparator(settings.binarySeparator);
        mySettingsComponent.setOctalSeparator(settings.octalSeparator);
        mySettingsComponent.setDecimalSeparator(settings.decimalSeparator);
        mySettingsComponent.setHexSeparator(settings.hexSeparator);
        mySettingsComponent.setMaxLen(settings.maxContentLen);
    }

    @Override
    public void disposeUIResources(){
        mySettingsComponent = null;
    }
}
