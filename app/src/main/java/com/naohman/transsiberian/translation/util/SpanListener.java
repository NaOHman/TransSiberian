package com.naohman.transsiberian.translation.util;

import android.view.View;

/**
 * Created by jeffrey on 1/16/15.
 * Interface for responding to clicks on definitions or references
 */
public interface SpanListener {
    /**
     * User clicked a reference link
     * @param v the view of that link
     * @param entry the entry corresponding to the reference
     * @param word the word itself
     */
    public abstract void onReferenceClick(View v, DictEntry entry, String word);

    /**
     * User clicked on a defnition link
     * @param v the view of the link
     * @param entry the entry corresponding to the definition
     * @param word the word itself
     */
    public abstract void onDefinitionClick(View v, DictEntry entry, String word);

    /**
     * User clicked on the keyword of the entry
     * @param v the view of that link
     * @param entry the entry the keyword belongs to
     */
    public abstract void onKeywordClick(View v, DictEntry entry);
}
