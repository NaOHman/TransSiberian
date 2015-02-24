package com.naohman.transsiberian.Study;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.naohman.transsiberian.Quizlet.Term;
import com.naohman.language.transsiberian.R;

public class AddTermFragment extends DialogFragment {
    public static String TERM_TAG = "term";
    private Term term = null;
    private EditText termET, defET;
    private Button newTerm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_term, container, false);
        defET = (EditText) v.findViewById(R.id.term_definition);
        termET = (EditText) v.findViewById(R.id.term_term);
        newTerm = (Button) v.findViewById(R.id.new_term);
        Bundle args = getArguments();
        if (args.getSerializable(TERM_TAG) != null) {
            term = (Term) args.getSerializable(TERM_TAG);
            getDialog().setTitle("Edit Term");
            defET.setText(term.getDefinition());
            termET.setText(term.getTerm());
            newTerm.setText("Save");
        } else {
            getDialog().setTitle("New Term");
        }
        newTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetActivity a = (SetActivity) getActivity();
                String title = termET.getText().toString();
                String definition = defET.getText().toString();
                boolean valid = a.addTerm(title, definition, term);
                if (valid)
                    dismiss();
            }
        });
        return v;
    }
}
