package com.naohman.transsiberian.Quizlet;

import java.io.Serializable;

/**
 * Created by jeffrey on 1/26/15.
 */
public class QuizletSet implements Serializable{
    private long _id;
    private long setId;
    private String title;
    private String description;
    private String lang_terms;
    private String lang_definitions;

    public QuizletSet(long _id, String title, String description,
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
}
