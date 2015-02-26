package com.naohman.transsiberian.Translation.Util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
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
    //TODO make spans visually distinct
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
        DictHeading h = new DictHeading(s, 0);
        spannable = h.toSpan(0, this);
    }

    public DictEntry(){ }

    /**
     * set the listener for the various spans
     * @param spanListener the listener
     */
    public void setSpanListener(SpanListener spanListener){
        listener = spanListener;
    }

    /**
     * get all the definitions contained in this entry
     * @return the list of definitions
     */
    public String[] getDefinitions(){
        return definitions.toArray(new String[definitions.size()]);
    }

    /**
     * @return a spanned string representing this entry
     */
    public Spanned getSpanned(){
        if (spannable == null)
            return new SpannedString("No Translations");
        return spannable;
    }

    /**
     * @return the keyword of this entry
     */
    public String getKeyword(){
        return keyword;
    }

    /**
     * handle an html tag
     * @param opening whether the tag is opening or closing
     * @param tag the text of the tag
     * @param output the String thus far
     * @param xmlReader the XMLReader associated with this handler
     */
    @Override
    public void handleTag(final boolean opening, final String tag,
                          Editable output, final XMLReader xmlReader){
        int l = output.length();
        //When we come across opening flags set marker flags on the output
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
        //When we find closing tags, create the appropriate span from the marker to the current position
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

    /**
     * Pull the actual definition out of the text and wrap it with the right span
     * @param text the output thus far
     * @param start the start of corresponding tag
     * @param end the end of the corresponding tag
     */
    private void handleDefSpans(Editable text, int start, int end){
        String full = text.toString();
        String target = full.substring(start, end);
        target = target.replaceAll("\\w+\\.","");
        List<String> defs = new ArrayList<>();
        List<String> spans = new ArrayList<>();
        String def = "", span = "", paren = "";
        boolean open = false, center = false;
        //Parcing Char by Char is the only reliable way to go
        for(int p=0; p<target.length(); p++){
            char c = target.charAt(p);
            //if parenthesis are open
            if (open) {
                //add char to the center section if needed to
                if (center)
                    paren += c;
                //if close parenthesis, close and break if at end
                if (c == ')'){
                    open = false;
                    if (p+1 != target.length() && !isBreak(target.charAt(p+1))) {
                        span += paren;
                    }
                }
            //Parenthesis aren't open
            } else {
               if (c == '(') {
                   open = true;
                   //Parenthesis are in the middle of the section, they could contain
                   //a particle such as (ся)
                   if (!def.matches("\\s*")){
                       center = true;
                       paren = "(";
                   }
                //if we reach a 'breaking character' set the span
               } else if (isBreak(c)) {
                   defs.add(def);
                   spans.add(span);
                   open = false; center = false;
                   def = ""; span = ""; paren = "";
               //plain old chars
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

    /**
     * @param c a character
     * @return whether the character would end a given section
     */
    private boolean isBreak(char c){
        return (c == ',' || c == ';' || c == '-');
    }

    /**
     * Get the last flag of it's kind in the entry
     * @param text the entry so far
     * @param kind the class of the flag
     * @return the position of the flag in the text
     */
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

    /**
     * a clickable span that holds a reference
     */
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

    /**
     * a Clickable span that holds a keyword
     */
    private class KeywordSpan extends ClickableSpan {
        public  KeywordSpan(){}
        @Override
        public void onClick(View view){
            listener.onKeywordClick(view, DictEntry.this);
        };
    }

    /**
     * a Clickable span that holds a definition
     */
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
