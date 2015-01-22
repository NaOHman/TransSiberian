package com.naohman.language.transsiberian.Helpers;

import android.view.View;

/**
 * Created by jeffrey on 1/16/15.
 * Interface for responding to clicks on definitions or references
 */
public interface SpanListener {
    public abstract void onReferenceClick(View v, String word);
    public abstract void onDefinitionClick(View v, String word);
}
