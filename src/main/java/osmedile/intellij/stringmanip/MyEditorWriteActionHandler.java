package osmedile.intellij.stringmanip;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @see <a href="https://github.com/krasa/StringManipulation/blob/master/src/main/java/osmedile/intellij/stringmanip/MyEditorWriteActionHandler.java">Reference</a>
 */
public abstract class MyEditorWriteActionHandler extends EditorActionHandler {



    public MyEditorWriteActionHandler() {
        super(false);
    }

    @Override
    protected final void doExecute(final Editor editor, @Nullable final Caret caret, final DataContext dataContext) {

        final Pair<Boolean, ?> additionalParameter = beforeWriteAction(editor, dataContext);
        if (!additionalParameter.first) {
            return;
        }

        final Runnable runnable = () -> {
            executeWriteAction(editor, dataContext, additionalParameter.second);
        };
        new EditorWriteActionHandler(false) {
            @Override
            public void executeWriteAction(Editor editor1, @Nullable Caret caret1, DataContext dataContext1) {
                runnable.run();
            }
        }.doExecute(editor, caret, dataContext);
    }



    protected abstract void executeWriteAction(Editor editor, DataContext dataContext, @Nullable Object additionalParameter);

    @NotNull
    protected Pair<Boolean, ?> beforeWriteAction(Editor editor, DataContext dataContext) {
        return continueExecution();
    }

    protected final Pair<Boolean, ?> stopExecution() {
        return new Pair<>(false, null);
    }


    protected final Pair<Boolean, ?> continueExecution() {
        return new Pair<>(true, null);
    }


}