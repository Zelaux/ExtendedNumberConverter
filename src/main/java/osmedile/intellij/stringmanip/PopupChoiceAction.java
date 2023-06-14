package osmedile.intellij.stringmanip;

import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.util.xmlb.annotations.Attribute;
import com.zelaux.numberconverter.actions.ExecutionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.deft.Obj;

import java.awt.*;
import java.awt.event.InputEvent;
/**
 * @see <a href="https://github.com/krasa/StringManipulation/blob/master/src/main/java/osmedile/intellij/stringmanip/PopupChoiceAction.java">Reference</a>
 * */
public class PopupChoiceAction extends MyEditorAction {
    @Attribute("actionGroupId")
    public String actionGroupId;

    public PopupChoiceAction() {
        super(null);
        setupHandler(new MyEditorWriteActionHandler<>(this) {
            @Override
            protected ExecutionResult<Object> beforeWriteAction(Editor editor, DataContext dataContext) {
                if (editor instanceof EditorEx) {
                    dataContext = ((EditorEx) editor).getDataContext();
                }//Evaluate compile-time expression checked
                ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(null, (ActionGroup) CustomActionsSchema.getInstance().getCorrectedAction("NumberManipulation.Group"),
                        dataContext, JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, true);

                popup.showInBestPositionFor(dataContext);
                return stopExecution();
            }

            @Override
            protected void executeWriteAction(Editor editor, DataContext dataContext, @Nullable Object additionalParameter) {

            }
        });
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
}