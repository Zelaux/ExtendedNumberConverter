package com.zelaux.numberconverter.settings;

import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.*;
import org.jetbrains.annotations.*;

@State(
name = "com.zelaux.numberconverter.settings.NumberTransformerSettingsState",
storages = @Storage("NumberTransformerSettings.xml")
)
public class MySettingsState implements PersistentStateComponent<MySettingsState>{


    public OutputCase outputCase=OutputCase.UpperCase;
    public int binarySeparator=4;
    public int octalSeparator=2;
    public int decimalSeparator=3;
    public int hexSeparator=2;
    public int maxContentLen=70;

    public static MySettingsState getInstance() {
        return ApplicationManager.getApplication().getService(MySettingsState.class);
    }

    @Nullable
    @Override
    public MySettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull MySettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
