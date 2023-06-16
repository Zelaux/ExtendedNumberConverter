package com.zelaux.numberconverter;

import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.kotlin.idea.intentions.SwapStringEqualsIgnoreCaseIntention;

public class Vars {
    public static final String pluginName="Number Manipulation";
    public static final String pluginId="NumberManipulation";
    public static PluginId pluginId(){
        return PluginId.getId(pluginId);
    }
}
