package com.naohman.language.transsiberian.Helpers;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;

import org.xml.sax.XMLReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffrey on 1/16/15.
 */
public class DictEntry implements Html.TagHandler {
    private static final int DELTA = 35;
    private SpanListener listener;
    private List<String> definitions = new ArrayList<>();
    private String keyword;
    private int indentLevel = -DELTA;
    private Spanned spannable;
    private boolean marker = false;

    public DictEntry(String s){
        s = s.replaceAll("<rref>[^<]+</rref>", ""); //remove reference to external resources
        s = s.replaceAll("\\\\n", "<br>");    //turn newline into html linebreak
//        s = DictHeading.parse(s, 0);
//        s = s.replaceAll("<br>\\s*<br>","<br>");
//        s = s.replaceAll("<br>\\s*</section><br>", "<br></section>"); //remove excessive line breaks
//        s = s.replaceAll("<br>\\s</section>", "<br></section>"); //remove excessive line breaks
        DictHeading h = new DictHeading(s, 0);
        spannable = h.toSpan(0, this);
    }

    public DictEntry(){ }

    public void setSpanListener(SpanListener spanListener){
        listener = spanListener;
    }

    public String[] getDefinitions(){
        return definitions.toArray(new String[definitions.size()]);
    }

    public Spanned getSpanned(){
        if (spannable == null)
            return new SpannedString("No Translations");
        return spannable;
    }

    public String getKeyword(){
        return keyword;
    }

    @Override
    public void handleTag(final boolean opening, final String tag,
                          Editable output, final XMLReader xmlReader){
        int l = output.length();
        if (tag.equalsIgnoreCase("section")){
            if (marker){
                int where = getLast(output, LeadingMarginSpan.Standard.class);
                output.setSpan(new LeadingMarginSpan.Standard(indentLevel, indentLevel),
                        where, l, 0);
                marker = false;
            }
            if (opening) {
                indentLevel += DELTA;
                marker = true;
            } else {
                marker = false;
                indentLevel -= DELTA;
            }
            output.setSpan(new LeadingMarginSpan.Standard(0,0), l, l, Spannable.SPAN_MARK_MARK);
        }
        if (opening){
           if (tag.equalsIgnoreCase("ex")){
               output.setSpan(new RelativeSizeSpan(0.8f),
                       output.length(), output.length(), Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("kref")){
               output.setSpan(new ReferenceLink(""),l,l, Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("k")){
               output.setSpan(new StyleSpan(Typeface.BOLD),l,l,Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("dtrn")){
               output.setSpan(new DefinitionLink(""), l,l,Spannable.SPAN_MARK_MARK);
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
               output.setSpan(new KeywordSpan(), where, l, 0);
               keyword = output.subSequence(where, l).toString();
           } else if (tag.equalsIgnoreCase("dtrn")){
               int where = getLast(output, DefinitionLink.class);
               handleDefSpans(output, where, l);
           }
        }
    }

    private void handleDefSpans(Editable text, int start, int end){
        //TODO handle (ся) split around some parenthesis/abbreviations
        String full = text.toString();
        String target = full.substring(start, end);
        target = target.replaceAll("\\w+\\.","");
        List<String> defs = new ArrayList<>();
        List<String> spans = new ArrayList<>();
        String def = "", span = "", paren = "";
        boolean open = false, center = false;
        for(int p=0; p<target.length(); p++){
            char c = target.charAt(p);
            if (open) {
                if (center)
                    paren += c;
                if (c == ')'){
                    open = false;
                    if (p+1 != target.length() && !isBreak(target.charAt(p+1))) {
                        span += paren;
                    }
                }
            } else {
               if (c == '(') {
                   open = true;
                   if (!def.matches("\\s*")){
                       center = true;
                       paren = "(";
                   }
               } else if (isBreak(c)) {
                   defs.add(def);
                   spans.add(span);
                   open = false; center = false;
                   def = ""; span = ""; paren = "";
               } else {
                   def += c;
                   span += c;
               }
            }
        }
        if (!def.matches("\\s*")){
            defs.add(def);
            spans.add(span);
        }
        for (int i=0; i<defs.size(); i++){
            def = defs.get(i).trim().replaceAll("  "," ");
            span = spans.get(i).trim();
            if(!def.matches("\\s*")){
                definitions.add(def);
                int s = full.indexOf(span, start);
                if (s> 0)
                    text.setSpan(new DefinitionLink(def),s, s+span.length(), 0);
            }
        }
    }

    private boolean isBreak(char c){
        return (c == ',' || c == ';' || c == '-');
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
                    if (j < 0) {
                        Log.d("Sub Zero Span", kind.toString());
                        return 0;
                    }
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
            listener.onReferenceClick(widget, DictEntry.this, link);
        }
    }

    private class KeywordSpan extends ClickableSpan {
        public  KeywordSpan(){}
        @Override
        public void onClick(View view){
            listener.onKeywordClick(view, DictEntry.this);
        };
    }

    private class DefinitionLink extends ClickableSpan {
        private String word;
        public DefinitionLink(String word) {
            this.word = word.replaceAll("\\(([^\\)]+)\\)", "");
        }
        @Override
        public void onClick(View widget) {
            listener.onDefinitionClick(widget, DictEntry.this, word);
        }

    }
}
