package com.zelaux.numberconverter.exceptions;

import arc.struct.Queue;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.zelaux.numberconverter.utils.IdeUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MyExceptionPopupManager {
    private static final Key<MyExceptionPopupManager> NUMBER_MANIPULATION_MY_EXCEPTION_MANAGER_KEY = Key.create("NUMBER_MANIPULATION_MY_EXCEPTION_MANAGER_KEY");
    public final Editor editor;

    private MyExceptionPopupManager(Editor editor) {
        this.editor = editor;
    }

    public static MyExceptionPopupManager getInstance(Editor editor) {
        MyExceptionPopupManager highlightManager = editor.getUserData(NUMBER_MANIPULATION_MY_EXCEPTION_MANAGER_KEY);
        if (highlightManager == null) {
            highlightManager = new MyExceptionPopupManager(editor);
            editor.putUserData(NUMBER_MANIPULATION_MY_EXCEPTION_MANAGER_KEY, highlightManager);
        }
        return highlightManager;
    }

    private final Queue<PopupDescriptor> list = new Queue<>();

    static class PopupDescriptor {
        public final Balloon balloon;
        public final RelativePoint relativePoint;
        public final Balloon.Position position;

        public PopupDescriptor(Balloon balloon, RelativePoint relativePoint, Balloon.Position position) {
            this.balloon = balloon;
            this.relativePoint = relativePoint;
            this.position = position;
        }

        public void run() {
            balloon.show(relativePoint, position);

        }
    }

    public void showErrorPopup(MyException myException) {
        myException.throwable.printStackTrace();
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(editor.getProject());
        Balloon balloon = JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(
                        myException.throwable + " <a href=\"enable\">Send bug report</a>",
                        MessageType.ERROR,
                        e -> {
                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                try {
                                    IdeUtils.reportConvertException(myException);
                                } catch (URISyntaxException | IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        })
                .setHideOnLinkClick(true)
                .createBalloon();
        RelativePoint relativePoint = RelativePoint.getCenterOf(statusBar.getComponent());
        Balloon.Position position = Balloon.Position.atRight;
        PopupDescriptor descriptor = new PopupDescriptor(balloon, relativePoint, position);
        list.addLast(descriptor);
        descriptor.run();
        balloon.addListener(new JBPopupListener() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent event) {
                list.removeFirst();//Removing balloon
                if (list.isEmpty()) return;
                list.first().run();
            }
        });

    }
}
