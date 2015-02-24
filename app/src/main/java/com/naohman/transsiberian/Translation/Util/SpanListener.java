package com.naohman.transsiberian.Translation.Util;

import android.view.View;

/**
 * Created by jeffrey on 1/16/15.
 * Interface for responding to clicks on definitions or references
 */
public interface SpanListener {
    public abstract void onReferenceClick(View v, DictEntry entry, String word);
    public abstract void onDefinitionClick(View v, DictEntry entry, String word);
    public abstract void onKeywordClick(View v, DictEntry entry);
}
