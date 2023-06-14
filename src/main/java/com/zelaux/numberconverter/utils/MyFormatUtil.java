package com.zelaux.numberconverter.utils;

import com.zelaux.numberconverter.settings.MySettingsState;

public class MyFormatUtil {
    public static String format(String text){
        int maxContentLen = MySettingsState.getInstance().maxContentLen;
        if(text.length()<= maxContentLen)return text;
        return text.substring(0,maxContentLen)+"...";
    }
}
