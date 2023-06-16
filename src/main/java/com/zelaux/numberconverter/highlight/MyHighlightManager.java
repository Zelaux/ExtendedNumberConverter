package com.zelaux.numberconverter.highlight;

import arc.struct.IntSet;
import arc.struct.ObjectMap;
import com.esotericsoftware.kryo.util.IntMap;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Key;
import com.zelaux.numberconverter.utils.IdeUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.jetbrains.kotlin.idea.util.DumbUtilsKt;

import java.util.ArrayList;
import java.util.List;

public class MyHighlightManager {
    public static final int HIGHLIGHT_LAYER = HighlighterLayer.SELECTION - 1;
    public static final TextAttributesKey SELECTED_NUMBER_EXPRESSION = TextAttributesKey.createTextAttributesKey("SELECTED_NUMBER_EXPRESSION");
    private static final Key<MyHighlightManager> NUMBER_MANIPULATION_HIGHLIGHT_MANAGER_KEY = Key.create("NUMBER_MANIPULATION_HIGHLIGHT_MANAGER_KEY");
    public final Editor editor;
    private final List<RangeHighlighter> highlighters = new ArrayList<>();
    private final IntMap<IntSet> highlighterRanges = new IntMap<IntSet>();
    private final IntMap<IntSet> highlighterRangesCopy = new IntMap<IntSet>();

    public MyHighlightManager(Editor editor) {
        this.editor = editor;
    }

    public static MyHighlightManager getInstance(Editor editor) {
        MyHighlightManager highlightManager = editor.getUserData(NUMBER_MANIPULATION_HIGHLIGHT_MANAGER_KEY);
        if (highlightManager == null) {
            highlightManager = new MyHighlightManager(editor);
            editor.putUserData(NUMBER_MANIPULATION_HIGHLIGHT_MANAGER_KEY, highlightManager);
        }
        return highlightManager;
    }

    public void removeHighlight() {
        MarkupModel markupModel = editor.getMarkupModel();
        for (RangeHighlighter highlighter : highlighters) {
            markupModel.removeHighlighter(highlighter);
            highlighter.dispose();
        }
        highlighters.clear();
        highlighterRanges.clear();
    }

    public void addHighlight(int startOffset, int endOffset) {
        if (DumbService.getInstance(editor.getProject()).isDumb()) return;
        IntSet intSet = highlighterRanges.get(startOffset);
        if (intSet == null) {
            highlighterRanges.put(startOffset, intSet = new IntSet());
        }
        if (!intSet.add(endOffset)) return;
        RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(SELECTED_NUMBER_EXPRESSION, startOffset, endOffset, HIGHLIGHT_LAYER,
                HighlighterTargetArea.EXACT_RANGE);
        highlighters.add(highlighter);
    }
}
