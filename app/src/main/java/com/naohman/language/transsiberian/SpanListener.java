package com.naohman.language.transsiberian;

import android.view.View;

/**
 * Created by jeffrey on 1/16/15.
 */
public interface SpanListener {
    public abstract void onReferenceClick(View v, String word);
    public abstract void onDefinitionClick(View v, String word);
}
