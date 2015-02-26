package com.naohman.transsiberian.Translation.Util;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.LeadingMarginSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffrey on 1/11/15.
 * a recursive structure that mirrors the format of dictionary entries
 * in the database. Currently, it's mostly used because it helps with
 * the formatting
 */
public class DictHeading {
    private static final int MAX_PREFIX = 4;
    private static final String[] FIRST_PREFIX = {"<b>I</b>", "<b>1\\.</b>","1\\)","а\\)","a\\)"};
    private String contents;
    private List<DictHeading> subHeadings;

    public DictHeading(String text, int prefixLevel){
    //no possible subheadings
        if (prefixLevel == MAX_PREFIX){
            this.contents = text;
            this.subHeadings = null;
            return;
        }
        this.subHeadings = new ArrayList<>();
        List<String> rawHeadings = new ArrayList<>();
        //Not every level of prefix gets used
        while (rawHeadings.size() < 2 && prefixLevel < MAX_PREFIX) {
            rawHeadings = breakHeadings(text, FIRST_PREFIX[prefixLevel], prefixLevel);
            prefixLevel++;
        }
        if (rawHeadings.size() > 0)
            this.contents = rawHeadings.get(0);
        else
            this.contents = text;
        if (rawHeadings.size() > 1){
            for (int i=1; i<rawHeadings.size(); i++){
                subHeadings.add(new DictHeading(rawHeadings.get(i), prefixLevel));
            }
        }
    }

    /**
     * break the text into a list of sections where the first section contains
     * the contents of a the text and the others, the text that forms the subHeadings
     * @param text the text to break
     * @param prefix the prefix to break on
     * @param prefixLevel the level of prefix ex. 2) 2. II B
     * @return the broken text
     */
    private static List<String> breakHeadings(String text, String prefix, int prefixLevel){
        List<String> headings = new ArrayList<>();
        //prefixes must be searched for in order because sometimes they appear in
        //a non-prefix role
        String[] chunks = text.split(prefix, 2);
        headings.add(chunks[0]);
        String unescaped = prefix.replaceAll("\\\\", "");
        if (chunks.length > 1)
            headings.addAll(breakHeadings(unescaped + chunks[1],
                    nextPrefix(prefixLevel, prefix), prefixLevel));
        return headings;
    }

    /**
     * Return a spannable string representation of this heading
     * @param indent the amount to indent each section
     * @param handler the tag handler used to convert the section
     * @return the spanned text
     */
    public SpannableStringBuilder toSpan(int indent, Html.TagHandler handler){
        SpannableStringBuilder s = (SpannableStringBuilder) Html.fromHtml("\u200B"+contents,null, handler);
        s.setSpan(new LeadingMarginSpan.Standard(indent,indent), 0, s.length(), 0);
        if (subHeadings != null) {
            for (DictHeading heading : subHeadings) {
                s.append(heading.toSpan(indent + 30, handler));
            }
        }
        return s;
    }

    /**
     * @param level the prefix level eg 2. 2) II. etc
     * @param current the current prefix
     * @return the next prefix on the same level ex. <b>II</b> => <b>III</b>
     */
    private static String nextPrefix(int level, String current){
        String next;
        switch (level) {
            case 0:
                next = current.replaceAll("</?b>", "");
                next = "<b>" + nextRomanNumeral(next) + "</b>";
                break;
            case 1:
                next = current.replaceAll("\\\\?\\.?</?b>", "");
                next = "<b>" + (Integer.parseInt(next) + 1) + "\\.</b>";
                break;
            case 2:
                next = current.substring(0, current.length() -2);
                next = "" + (Integer.parseInt(next) + 1) + "\\)";
                break;
            default:
                next = current.substring(0, current.length() -2);
                next = ((char) (next.charAt(0) + 1)) + "\\)";
                break;
        }
        return next;
    }

    /**
     * ONLY WORKS FOR VALUES < 40!!!
     * @param current the current roman numeral
     * @return the next roman numeral
     */
    private static String nextRomanNumeral(String current){
        current += 'I';
        current = current.replaceAll("IIII","IV");
        current = current.replaceAll("IVI","V");
        current = current.replaceAll("VIIII","IX");
        current = current.replaceAll("IXI","X");
        return  current;
    }
}
