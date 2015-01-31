package com.naohman.language.transsiberian.Helpers;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.LeadingMarginSpan;
import android.util.Log;

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

    public static String parse(String text, int prefixLevel){
        if (text == "")
            return "";
        String parsed;
        List<String> sections = breakHeadings(text, FIRST_PREFIX[prefixLevel], prefixLevel);
        //Not every level of prefix gets used
        while (sections.size() < 2 && prefixLevel < MAX_PREFIX) {
            prefixLevel++;
            sections = breakHeadings(text, FIRST_PREFIX[prefixLevel], prefixLevel);
        }
        if (sections.size() > 0 && prefixLevel < MAX_PREFIX)
            parsed = sections.get(0);
        else
            parsed = text;
        if (sections.size() > 1){
            for (int i=1; i<sections.size(); i++){
                parsed += parse(sections.get(i), prefixLevel+1);
            }
        }
        if (prefixLevel == 0)
            return "<section>" + parsed + "</section>";
        else
            return "<br><section>" + parsed + "</section>";
    }

    /*
     * break the text into a list of sections where the first section contains
     * the contents of a the text and the others, the text that forms the subHeadings
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

    /*
     * generates the next prefix given the prefix level and the
     * current prefix. ex. <b>II</b> => <b>III</b>
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

    /*
     * ONLY WORKS FOR VALUES < 40!!!
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
