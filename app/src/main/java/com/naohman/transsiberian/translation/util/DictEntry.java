package com.naohman.transsiberian.translation.util;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.setUp.App;

import org.xml.sax.XMLReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffrey on 1/16/15.
 * Represents a dictionary entry in the translation database
 */
public class DictEntry implements Html.TagHandler {
    private SpanListener listener;
    private List<String> definitions = new ArrayList<>();
    private String keyword;
    private Spanned spannable;

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
               output.setSpan(new ReferenceSpan(""),l,l, Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("k")){
               output.setSpan(new KeywordSpan(),l,l,Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("dtrn")){
               output.setSpan(new DefinitionSpan(""), l,l,Spannable.SPAN_MARK_MARK);
           }
        //When we find closing tags, create the appropriate span from the marker to the current position
        } else {
           if (tag.equalsIgnoreCase("ex")){
               int where = getLast(output, RelativeSizeSpan.class);
               output.setSpan(new ForegroundColorSpan(App.context().getResources().
                               getColor(R.color.ex_color)),where, l, 0);
               output.setSpan(new RelativeSizeSpan(0.8f), where, l, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
           } else if (tag.equalsIgnoreCase("kref")){
               int where = getLast(output, ReferenceSpan.class);
               output.setSpan(new ReferenceSpan(output.subSequence(where, l)), where, l,
                       Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
           } else if (tag.equalsIgnoreCase("k")){
               int where = getLast(output, KeywordSpan.class);
               output.setSpan(new KeywordSpan(), where, l, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
               keyword = output.subSequence(where, l).toString();
           } else if (tag.equalsIgnoreCase("dtrn")){
               int where = getLast(output, DefinitionSpan.class);
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
        //Parsing Char by Char is the only reliable way to go
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
                    text.setSpan(new DefinitionSpan(def),s, s+span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
    private class ReferenceSpan extends ClickableSpan {
        private String link;
        public ReferenceSpan(CharSequence link) {
            this.link = link.toString();
        }
        @Override
        public void onClick(View widget) {
            listener.onReferenceClick(widget, DictEntry.this, link);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            int color = App.context().getResources().getColor(R.color.reference_color);
            ds.setColor(color);
        }
    }

    /**
     * a Clickable span that holds a keyword
     */
    private class KeywordSpan extends ClickableSpan {

        public KeywordSpan() {}

        @Override
        public void onClick(View view) {
            listener.onKeywordClick(view, DictEntry.this);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            Resources r = App.context().getResources();
            ds.setTextSize(r.getDimensionPixelSize(R.dimen.heading));
            ds.setColor(r.getColor(R.color.primary_text_default_material_light));
            ds.setUnderlineText(false);
        }
    }

    /**
     * a Clickable span that holds a definition
     */
    private class DefinitionSpan extends ClickableSpan {
        private String word;

        public DefinitionSpan(String word) {
            this.word = word.replaceAll("\\(([^\\)]+)\\)", "");
        }

        @Override
        public void onClick(View widget) {
            listener.onDefinitionClick(widget, DictEntry.this, word);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            int color = App.context().getResources().getColor(R.color.definition_color);
            ds.setColor(color);
        }
    }
}
