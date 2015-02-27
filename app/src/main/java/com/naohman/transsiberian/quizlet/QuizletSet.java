package com.naohman.transsiberian.quizlet;

import android.util.Log;

import com.naohman.language.transsiberian.R;

import java.io.Serializable;

/**
 * Created by jeffrey on 1/26/15.
 * POJO that Represents a quizlet set. There is no fancy logic here.
 */
public class QuizletSet implements Serializable{
    private long _id;
    private long setId;
    private String title;
    private String description;
    private String lang_terms;
    private String lang_definitions;

    protected QuizletSet(long _id, String title, String description,
                      String lang_terms, String lang_definitions, long setId) {
        this.title = title;
        this.description = description;
        this.lang_terms = lang_terms;
        this.lang_definitions = lang_definitions;
        this._id = _id;
        this.setId = setId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long get_id() {
        return _id;
    }

    public String getLang_definitions() {
        return lang_definitions;
    }

    public String getLang_terms() {
        return lang_terms;
    }

    public int getLang_termsPretty(){
        Log.d("Term Language", lang_terms);
        if (lang_terms.equals(Quizlet.RUSSIAN)) {
            Log.d("terms", "Russian");
            return R.string.russian_terms;
        } else {
            Log.d("terms", "English");
            return R.string.english_terms;
        }
    }

    public int getLang_definitionsPretty(){
        Log.d("Def Language", lang_definitions);
        if (lang_definitions.equals(Quizlet.RUSSIAN)) {
            Log.d("defs", "Russian");
            return R.string.russian_defs;
        } else {
            Log.d("defs", "English");
            return R.string.english_defs;
        }
    }
}
