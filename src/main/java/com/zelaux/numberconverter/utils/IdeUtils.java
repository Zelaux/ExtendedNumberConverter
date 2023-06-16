package com.zelaux.numberconverter.utils;

import arc.util.Structs;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.zelaux.numberconverter.Vars;
import com.zelaux.numberconverter.exceptions.MyException;
import kotlin.Unit;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

public class IdeUtils {

    public static class Language {
        public static com.intellij.lang.Language from(com.intellij.openapi.vfs.VirtualFile file) {
            return LanguageUtil.getFileLanguage(file);
        }

        public static com.intellij.lang.Language from(com.intellij.openapi.editor.Editor editor) {
            return LanguageUtil.getFileLanguage(VirtualFile.from(editor));
        }

        public static com.intellij.lang.Language from(AnActionEvent anActionEvent) {
            return LanguageUtil.getFileLanguage(VirtualFile.from(anActionEvent));
        }
    }

    public static class VirtualFile {
        public static @Nullable com.intellij.openapi.vfs.VirtualFile from(com.intellij.openapi.editor.Editor editor) {
            return FileDocumentManager.getInstance().getFile(editor.getDocument());
        }

        public static @Nullable com.intellij.openapi.vfs.VirtualFile from(AnActionEvent anActionEvent) {
            return anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        }
    }

    public static class PsiFile {
        public static com.intellij.psi.PsiFile from(com.intellij.openapi.project.Project project, com.intellij.openapi.vfs.VirtualFile virtualFile) {
            return PsiManager.getInstance(project).findFile(virtualFile);
        }

        public static com.intellij.psi.PsiFile from(AnActionEvent anActionEvent) {
            return from(IdeUtils.Project.from(anActionEvent), VirtualFile.from(anActionEvent));
        }

        public static com.intellij.psi.PsiFile from(com.intellij.openapi.editor.Editor editor) {
            return IdeUtils.PsiFile.from(editor.getProject(), IdeUtils.VirtualFile.from(editor));
        }
    }


    public static class Editor {
        public static com.intellij.openapi.editor.Editor from(AnActionEvent anActionEvent) {
            return anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        }
    }

    public static class Project {
        public static com.intellij.openapi.project.Project from(AnActionEvent anActionEvent) {
            return anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        }
    }

    public static void reportConvertException(MyException exception) throws URISyntaxException, IOException {
        reportConvertException(exception.throwable, exception.inElementStart, exception.inElementEnd, exception.language, exception.element);
    }
    private static void reportConvertException(Throwable throwable, int inElementStart, int inElementEnd, com.intellij.lang.Language language, PsiElement psiElement) throws URISyntaxException, IOException {
        throwable.printStackTrace();
        IndentBuilder info = new IndentBuilder(" ");
        info.separator = "\n";
        ApplicationInfo appInfo = ApplicationInfo.getInstance();

        getAppName(info, appInfo);
        info.println();
        addBuild(info, appInfo);
        info.println();
        IdeaPluginDescriptor[] allPlugins = Arrays.stream(PluginManagerCore.getPlugins())
                .filter(it -> it.getVendor() != null && !it.getVendor().equals("JetBrains"))
                .collect(Collectors.toList()).toArray(IdeaPluginDescriptor[]::new);
        info.println("Plugins: ");
        info.indent(() -> {
            for (IdeaPluginDescriptor plugin : allPlugins) {
                info.print("- ");
                info.print(plugin.getPluginId());
                info.print("(v ");
                info.print(plugin.getVersion());
                info.println(")");
            }
            /*IdeaPluginDescriptor myPlugin = PluginManagerCore.getPlugin(Vars.pluginId());*/
        });
        info.println();

        info.println("PsiElement: ");
        info.indent(() -> {
            info.println("- text: '" + psiElement.getText() + "'");
            info.println("- string: " + psiElement );
            info.println("- class: " + psiElement.getClass().getName());
            info.println("- lang: " + language.getID());
            info.println("- dcursor(" + inElementStart+","+inElementEnd+")");
        });
        info.println();

        info.println("Exception: ");
        {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            int index = -1;
            for (int i = stackTrace.length - 1; i >= 0; i--) {
                if (stackTrace[i].toString().contains("zelaux")) {
                    index = i;
                    break;
                }
            }
            if(index!=-1){
                StackTraceElement[] stackTraceElements = new StackTraceElement[index + 1];
//                System.arraycopy(stackTraceElements, 0, throwable.getStackTrace(), 0, stackTraceElements.length);
                for (int i = 0; i < stackTraceElements.length; i++) {
                    int counter=0;
                    while (stackTrace[i]==null && counter<100){
                        stackTrace=throwable.getStackTrace();
                        counter++;
                    }
                    stackTraceElements[i]=stackTrace[i];
                }
                throwable.setStackTrace(stackTraceElements);
            }
            throwable.printStackTrace(info);
        }
        System.out.println("Body: \n" + info.toString());
        URI uri = new URIBuilder("https://github.com/Zelaux/ExtendedNumberConverter/issues/new").
                addParameter("title", throwable.toString()).
                addParameter("body", info.toString()).build();
        Desktop.getDesktop().browse(uri);
    }

    private static void addBuild(IndentBuilder builder, ApplicationInfo appInfo) {
        String buildInfoNonLocalized = MessageFormat.format("Build #{0}", appInfo.getBuild().asString());
        Date timestamp = appInfo.getBuildDate().getTime();
        if (appInfo.getBuild().isSnapshot()) {
            String time = new SimpleDateFormat("HH:mm").format(timestamp);
            buildInfoNonLocalized += MessageFormat.format(", built on {0} at {1}",
                    DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(timestamp), time);
        } else {
            buildInfoNonLocalized += MessageFormat.format(", built on {0}",
                    DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(timestamp));
        }
        builder.append(buildInfoNonLocalized);
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private static void getAppName(IndentBuilder builder, ApplicationInfo appInfo) {
        ApplicationNamesInfo namesInfo = ApplicationNamesInfo.getInstance();
        String appName = appInfo.getFullApplicationName();
        String edition = namesInfo.getEditionName();
        if (edition != null) appName += " (" + edition + ")";
        builder.append("IDE: " + appName);

    }
}
