package com.zelaux.numberconverter.actions;

import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.IdeFocusManager;
import com.zelaux.numberconverter.highlight.MyHighlightManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.InputEvent;

public class PopupChoiceAction extends MyEditorAction {
    public String actionGroupId;

    public PopupChoiceAction() {
        super(null);
        setupHandler(new MyEditorWriteActionHandler<>(this) {
            @Override
            protected ExecutionResult<Object> beforeWriteAction(Editor editor, DataContext dataContext) {
                if (editor instanceof EditorEx) {
                    dataContext = ((EditorEx) editor).getDataContext();
                }//Evaluate compile-time expression checked
                ListPopup popup = JBPopupFactory.getInstance()
                        .createActionGroupPopup(null,
                                (ActionGroup) CustomActionsSchema.getInstance().getCorrectedAction(actionGroupId),
                                dataContext,
                                JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING,
                                true,
                                () -> {
//                                    MyHighlightManager.getInstance(editor).removeHighlight();
                                },
                                -1,
                                null,
                                null);


               popup.addListener(new JBPopupListener() {

                    @Override
                    public void onClosed(@NotNull LightweightWindowEvent event) {
                        JBPopupListener.super.onClosed(event);
                        MyHighlightManager.getInstance(editor).removeHighlight();
                    }
                });
                 /*popup.addListSelectionListener(e->{
                    MyHighlightManager.getInstance(editor).removeHighlight();
                });*/
                popup.showInBestPositionFor(dataContext);
                return stopExecution();
            }

            @Override
            protected void executeWriteAction(Editor editor, DataContext dataContext, @Nullable Object additionalParameter) {

            }
        });
    }

    public static boolean isFromDialog(Project project) {
        if (!EventQueue.isDispatchThread()) {
            return false; //probably Search Everywhere
        }
        final Component owner = IdeFocusManager.getInstance(project).getFocusOwner();
        if (owner != null) {
            final DialogWrapper instance = DialogWrapper.findInstance(owner);
            return instance != null;
        }
        return false;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        if (editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        Project project = getEventProject(e);
        if (project != null) {
            InputEvent inputEvent = e.getInputEvent();
            boolean onlyAltDown = false;
            if (inputEvent != null) {
                onlyAltDown = inputEvent.isAltDown() && !inputEvent.isShiftDown() && !inputEvent.isMetaDown() && !inputEvent.isControlDown();
            }
            LookupEx activeLookup = LookupManager.getInstance(project).getActiveLookup();
            boolean dialogOpen = isFromDialog(project);
            boolean popupCheck = activeLookup == null || !onlyAltDown;
            boolean dialogCheck = !dialogOpen || !onlyAltDown;
            e.getPresentation().setEnabled((popupCheck && dialogCheck));
        }
    }
}