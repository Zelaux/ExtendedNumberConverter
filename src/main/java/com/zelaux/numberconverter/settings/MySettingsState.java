package com.zelaux.numberconverter.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.settings.processor.Separator;
import com.zelaux.numberconverter.settings.processor.SettingField;
import com.zelaux.numberconverter.settings.processor.SettingsFieldProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "com.zelaux.numberconverter.settings.NumberTransformerSettingsState",
        storages = @Storage("NumberTransformerSettings.xml")
)
public class MySettingsState implements PersistentStateComponent<MySettingsState> {

    public final SettingsFieldProcessor processor = new SettingsFieldProcessor(MySettingsState.class);

    @SettingField(title = "Binary separator spicing", component = "intField(1,1024)")
    public int binarySeparator = 4;
    @SettingField(title = "Octal separator spicing", component = "intField(1,1024)")
    public int octalSeparator = 2;
    @SettingField(title = "Decimal separator spicing", component = "intField(1,1024)")
    public int decimalSeparator = 3;
    @SettingField(title = "Hexadecimal separator spicing", component = "intField(1,1024)")
    public int hexSeparator = 2;
    @Separator
    @SettingField(title = "Max message length", component = "intField(1,1024)")
    public int maxContentLen = 70;
    @Separator
    @SettingField(
            title = "Radix of value to shift in shift operations: ",
            component = "comboBox(ShiftRadix.all)"
    )
    public ShiftRadix shiftRadix = ShiftRadix.of(DefaultRadixNumberType.Binary);
    @Separator
    @SettingField(
            title = "Output case: ",
            component = "comboBox(OutputCase.values())"
    )
    public OutputCase outputCase = OutputCase.UpperCase;

    public MySettingsState() {
    }

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
