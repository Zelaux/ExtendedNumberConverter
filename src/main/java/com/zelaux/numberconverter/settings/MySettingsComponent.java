package com.zelaux.numberconverter.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.fields.IntegerField;
import com.intellij.util.ui.*;

import javax.swing.*;

public class MySettingsComponent {

    private final JPanel myMainPanel;
    //    private final JBTextField myUserNameText = new JBTextField();
//    private final JBCheckBox myColorExprSeqAsList = new JBCheckBox("View color expression sequence as list? ");
    private final IntegerField myBinarySeparator = new IntegerField("Binary separator spacing", 1, 1024);
    private final IntegerField myOctalSeparator = new IntegerField("Octal separator spacing", 1, 1024);
    private final IntegerField myDecimalSeparator = new IntegerField("Decimal separator spacing", 1, 1024);
    private final IntegerField myHexSeparator = new IntegerField("Hexadecimal separator spacing", 1, 1024);
    private final IntegerField myMaxLen = new IntegerField("max len", 5, 1024);
    public ComboBox<OutputCase> myOutputCase = new ComboBox<>(OutputCase.values());


    public MySettingsComponent() {
//        myBinarySeparator.di
        myMainPanel = FormBuilder.createFormBuilder()
//        .addLabeledComponent(new JBLabel("Enter user name: "), myUserNameText, 1, false)
                .addLabeledComponent("Binary separator spicing", myBinarySeparator)
                .addLabeledComponent("Octal separator spicing", myOctalSeparator)
                .addLabeledComponent("Decimal separator spicing", myDecimalSeparator)
                .addLabeledComponent("Hex separator spicing", myHexSeparator)
                .addSeparator(1)
                .addLabeledComponent("Max message length", myHexSeparator)
                .addSeparator(1)
                .addLabeledComponent("Output case: ", myOutputCase, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return myMainPanel;
    }

    public int getDecimalSeparator() {
        return myDecimalSeparator.getValue();
    }

    public int getBinarySeparator() {
        return myBinarySeparator.getValue();
    }

    public int getOctalSeparator() {
        return myOctalSeparator.getValue();
    }

    public int getHexSeparator() {
        return myHexSeparator.getValue();
    }
    public int getMaxLen() {
        return myMaxLen.getValue();
    }

    public void setDecimalSeparator(int value) {
        myDecimalSeparator.setValue(value);
    }

    public void setBinarySeparator(int value) {
        myBinarySeparator.setValue(value);
    }

    public void setOctalSeparator(int value) {
        myOctalSeparator.setValue(value);
    }

    public void setMaxLen(int value) {
        myMaxLen.setValue(value);
    }

    public void setHexSeparator(int value) {
        myHexSeparator.setValue(value);
    }

    public OutputCase getOutputCase() {
        return (OutputCase) myOutputCase.getSelectedItem();
    }

    public void setOutputCase(OutputCase value) {
        myOutputCase.setSelectedItem(value);
    }
}
