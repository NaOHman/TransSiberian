package com.naohman.language.transsiberian.Helpers;

/**
 * Created by jeffrey on 1/26/15.
 */
public class Term {
    private long _id;
    private String term;
    private String definition;
    private long setId;
    private long quizlet_id;

    public Term(long _id, String term, String definition, long setId, long quizlet_id) {
        this._id = _id;
        this.term = term;
        this.definition = definition;
        this.setId = setId;
        this.quizlet_id = quizlet_id;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }
    public long get_id(){
        return _id;
    }
}
