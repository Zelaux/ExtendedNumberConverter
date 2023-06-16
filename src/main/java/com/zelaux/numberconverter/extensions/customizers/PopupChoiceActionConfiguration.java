package com.zelaux.numberconverter.extensions.customizers;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.ActionConfigurationCustomizer;
import com.zelaux.numberconverter.extensionpoints.PopupChoiceBean;
import org.jetbrains.annotations.NotNull;
import com.zelaux.numberconverter.actions.PopupChoiceAction;

public class PopupChoiceActionConfiguration implements ActionConfigurationCustomizer {
    @Override
    public void customize(@NotNull ActionManager actionManager) {
        for (PopupChoiceBean extension : PopupChoiceBean.EP_NAME.getExtensions()) {
            String actionId = extension.id == null ? extension.actionGroup + "PopupChoice" : extension.id;

            ActionGroup parentGroup = (ActionGroup) actionManager.getAction(extension.actionGroup);
            String text = parentGroup.getTemplatePresentation().getText();

            PopupChoiceAction newAction = new PopupChoiceAction();
            newAction.actionGroupId= extension.actionGroup;
            Presentation templatePresentation = newAction.getTemplatePresentation();
            templatePresentation.setText(extension.text == null ? "Show Popup Choice For " + text : extension.text);
            if (extension.description != null) {
                templatePresentation.setDescription(extension.description);
            }

            actionManager.registerAction(actionId, newAction);
            for (PopupChoiceBean.ShortcutDescriptor shortcut : extension.shortcuts()) {
                shortcut.apply(actionId);
            }
        }
    }
}
