package com.naohman.language.transsiberian;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;

import org.xml.sax.XMLReader;

/**
 * Created by jeffrey on 1/16/15.
 */
public class DictTagHandler implements Html.TagHandler {
    private SpanListener listener;

    public DictTagHandler(SpanListener l){
        this.listener = l;
    }

    @Override
    public void handleTag(final boolean opening, final String tag,
                          Editable output, final XMLReader xmlReader){
        int l = output.length();
        if (opening){
           if (tag.equalsIgnoreCase("ex")){
               output.setSpan(new RelativeSizeSpan(0.8f),
                       output.length(), output.length(), Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("kref")){
               output.setSpan(new ReferenceLink(""),l,l, Spannable.SPAN_MARK_MARK);

           } else if (tag.equalsIgnoreCase("k")){
               output.setSpan(new StyleSpan(Typeface.BOLD),l,l,Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("dtrn")){
               output.setSpan(new SpeechLink(""), l,l,Spannable.SPAN_MARK_MARK);
           }
        } else {
           if (tag.equalsIgnoreCase("ex")){
               int where = getLast(output, RelativeSizeSpan.class);
               output.setSpan(new ForegroundColorSpan(Color.parseColor("#aaaaaa")),
                       where, l, 0);
               output.setSpan(new RelativeSizeSpan(0.8f), where, l, 0);
           } else if (tag.equalsIgnoreCase("kref")){
               int where = getLast(output, ReferenceLink.class);
               output.setSpan(new ReferenceLink(output.subSequence(where, l)), where, l, 0);
           } else if (tag.equalsIgnoreCase("k")){
               int where = getLast(output, StyleSpan.class);
               output.setSpan(new StyleSpan(Typeface.BOLD), where, l, 0);
               output.setSpan(new RelativeSizeSpan(1.25f), where, l, 0);
               handleTtsSpans(output, where, l);
           } else if (tag.equalsIgnoreCase("dtrn")){
               int where = getLast(output, SpeechLink.class);
               handleTtsSpans(output, where, l);
           }
        }
    }

    private void handleTtsSpans(Editable text, int start, int end){
        String full = text.toString();
        String target = full.substring(start, end);
        String trimmed = target.replaceAll("\\(([^\\)]+)\\)", "");
        trimmed = trimmed.replaceAll("\\w+\\.","");
        trimmed = trimmed.replaceAll("[a-zA-Z]","");
        String[] defs = trimmed.split(";|,|-");
        for (String def: defs){
            def = def.trim();
            if(TranslationService.isRussian(def) && def.length() > 0){
                int s = full.indexOf(def, start);
                text.setSpan(new SpeechLink(def),s, s+def.length(), 0);
            }
        }
    }

    private int getLast(Editable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if(objs.length == 0) {
            return 0;
        } else {
            for (int i=objs.length; i > 0; i--) {
                if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
                    int j = text.getSpanEnd(objs[i-1]);
                    text.removeSpan(objs[i-1]);
                    if (j < 0)
                        return 0;
                    return j;
                }
            }
            return 0;
        }
    }

    private class ReferenceLink extends ClickableSpan {
        private String link;
        public ReferenceLink(CharSequence link) {
            this.link = link.toString();
        }
        @Override
        public void onClick(View widget) {
            Log.d("Link Clicked", link);
            listener.onReferenceClick(widget, link);
        }
    }

    private class SpeechLink extends ClickableSpan {
        private String word;
        public SpeechLink(String word) {
            this.word = word;
            Log.d("Made Speech Link", word + word.length());
        }
        @Override
        public void onClick(View widget) {
            listener.onDefinitionClick(widget, word);
        }

    }
}
