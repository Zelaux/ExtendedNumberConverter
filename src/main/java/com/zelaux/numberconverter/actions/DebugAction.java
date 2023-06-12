package com.zelaux.numberconverter.actions;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.IncorrectOperationException;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.Result;
import com.zelaux.numberconverter.numbertype.DefaultRadixNumberType;
import com.zelaux.numberconverter.numbertype.NumberType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.List;

public class DebugAction extends AnAction {
    public DebugAction() {
    }

    @Override
    public final @NotNull ActionUpdateThread getActionUpdateThread() {
        //https://github.com/krasa/StringManipulation/issues/182
        //Access is allowed from event dispatch thread only exception is thrown from MyEditorAction.findActiveSpeedSearchTextField in IntelliJ 2022.3
        return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);

        final Document document = editor.getDocument();

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        final Language language = LanguageUtil.getFileLanguage(virtualFile);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        CaretModel caretModel = editor.getCaretModel();
        List<Caret> caretList = caretModel.getAllCarets();

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        for (Caret caret : caretList) {//caret.getLeadSelectionPosition()
            ;

            int selectionStart = caret.getSelectionStart();
            int selectionEnd = caret.getSelectionEnd();
            PsiElement element;
            PsiElement beginElement = psiFile.findElementAt(selectionStart);
            PsiElement endElement = psiFile.findElementAt(selectionEnd);
            ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
            TokenSet whitespaceTokens = parserDefinition.getWhitespaceTokens();
            if (beginElement == null && endElement == null) {
                continue;
            } else if (beginElement == null) {
                element = endElement;
            } else if (endElement == null) {
                element = beginElement;
            } else {
                while (whitespaceTokens.contains(beginElement.getNode().getElementType()) && selectionStart <= selectionEnd) {
                    beginElement = psiFile.findElementAt(++selectionStart);
                }
                while (whitespaceTokens.contains(endElement.getNode().getElementType()) && selectionStart <= selectionEnd) {
                    endElement = psiFile.findElementAt(--selectionEnd);
                }
                if (whitespaceTokens.contains(beginElement.getNode().getElementType()) && whitespaceTokens.contains(endElement.getNode().getElementType())) {
                    continue;
                }
                if (beginElement.getTextOffset() + beginElement.getTextLength() == selectionEnd) {
                    element = beginElement;
                } else {
                    element = PsiTreeUtil.findCommonParent(beginElement, endElement);
                }
            }
            int inElementStart = selectionStart - element.getTextOffset();
            int inElementEnd = selectionEnd - element.getTextOffset();
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            StringBuilder htmlContent = new StringBuilder();
            htmlContent
                    .append(element)
                    .append("(")
                    .append(element.getText())
                    .append(")(")
                    .append(inElementStart)
                    .append(", ")
                    .append(inElementEnd)
                    .append(")")
            ;
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(htmlContent.toString(), MessageType.INFO, e -> {})
                    .createBalloon()
                    .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
            String dialog;
            while (true) {
                dialog = Messages.showInputDialog(project, "write test code", "DEBUG", Messages.getInformationIcon());
                if (dialog != null && !dialog.isEmpty()) break;
            }
            Lexer lexer = parserDefinition.createLexer(project);
            lexer.start(dialog, 0, dialog.length());
            ASTFactory astFactory = LanguageASTFactory.INSTANCE.forLanguage(language);
            PsiElement first = null;
            StringBuilder builder = new StringBuilder();
            StringBuilder errors = new StringBuilder();

            while (true) {
                IElementType tokenType = lexer.getTokenType();
                if (tokenType == null) break;
                builder.append(tokenType.getDebugName() + "(" + tokenType.getIndex() + "),");
                try {
                    LeafElement leaf = astFactory.createLeaf(tokenType, lexer.getTokenSequence().toString());
                    if (first == null) {
                        first = leaf.getPsi();
                    } else {
                        first = first.add(leaf.getPsi());
                    }
                } catch (Throwable e) {
                    System.err.print("Token: "+tokenType);
                    e.printStackTrace();
                    errors.append("["+tokenType.getDebugName()+":"+tokenType.getIndex()+"]"+e.getClass().getCanonicalName()+": "+e.getMessage());
                }
                lexer.advance();
            }
            Balloon infoBaloon = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(builder.toString(), MessageType.INFO, e -> {
                    })
                    .createBalloon();
            infoBaloon.show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
            infoBaloon.addListener(new JBPopupListener() {
                @Override
                public void onClosed(@NotNull LightweightWindowEvent event) {
                    JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(errors.toString(), MessageType.ERROR, e -> {})
                            .createBalloon()
                            .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.above);
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {

    }
}
