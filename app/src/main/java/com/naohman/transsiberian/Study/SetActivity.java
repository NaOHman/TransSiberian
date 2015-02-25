package com.naohman.transsiberian.Study;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.naohman.transsiberian.Quizlet.QuizletSet;
import com.naohman.transsiberian.Quizlet.Term;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.Quizlet.Quizlet;

import java.util.List;

/**
 * An activity that displays information about a set an allows
 * users to edit their terms
 */
public class SetActivity extends ActionBarActivity {
    private QuizletSet mySet;
    private Quizlet quizlet;
    private TextView title_tv, description_tv;
    private ListView term_view;
    private List<Term> terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        mySet = (QuizletSet) getIntent().getSerializableExtra("set");
        title_tv = (TextView) findViewById(R.id.set_title);
        title_tv.setText(mySet.getTitle());
        description_tv = (TextView) findViewById(R.id.set_description);
        description_tv.setText(mySet.getDescription());
        term_view = (ListView) findViewById(R.id.term_lv);
        quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        terms = quizlet.getSetTerms(mySet.get_id());
        term_view.setAdapter(new TermListAdapter(this, R.layout.set_list_item, terms));
    }

    /**
     * Private class representing the new term and edit term dialogs
     */
    private class TermDialog extends AlertDialog {
        private EditText defET, termET;
        private final Term oldTerm;
        public TermDialog(Term oldTerm) {
            super(SetActivity.this);
            setUp();
            this.oldTerm = oldTerm;
            defET.setText(oldTerm.getDefinition());
            termET.setText(oldTerm.getTerm());
            setButton(BUTTON_POSITIVE, getString(R.string.save), (Message) null);
        }

        public TermDialog() {
            super(SetActivity.this);
            setUp();
            oldTerm = null;
            setButton(BUTTON_POSITIVE, getString(R.string.save_and_exit), (Message) null);
            setButton(BUTTON_NEUTRAL, getString(R.string.save), (Message) null);
        }

        private void setUp() {
            View v = getLayoutInflater().inflate(R.layout.fragment_add_term, null, false);
            defET = (EditText) v.findViewById(R.id.term_definition);
            termET = (EditText) v.findViewById(R.id.term_term);
            setView(v);
            setCancelable(false);
            setButton(BUTTON_NEGATIVE, getString(R.string.cancel), (Message) null);
            defET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        if (oldTerm == null) {
                            TermDialog.this.getButton(BUTTON_NEUTRAL).callOnClick();
                            focusKeyboard(termET);
                        } else {
                            TermDialog.this.getButton(BUTTON_POSITIVE).callOnClick();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        /**
         * Onclick methods are overridden here so that they prevent dialog from closing
         */
        @Override
        public void show() {
            super.show();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            focusKeyboard(termET);
            getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean success = addTerm(termET.getText().toString(),
                            defET.getText().toString(), oldTerm);
                    if (success)
                        dismiss();
                }
            });
            if (oldTerm == null){
                getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean success = addTerm(termET.getText().toString(),
                                defET.getText().toString(), null);
                        if (success){
                            defET.setText("");
                            termET.setText("");
                        }
                    }
                });
            }
        }

        @Override
        protected void onStop() {
//            SetActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            super.onStop();
        }
    }

    public void focusKeyboard(View v){
        if (v.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * adds a new term to the set, alerts user if the input has an error
     * @param term the term keyword
     * @param definition the term definition
     * @param oldTerm the term being edited
     * @return whether the term was successfully added
     */
    public boolean addTerm(String term, String definition, Term oldTerm){
        if (term.matches("\\s*") || definition.matches("\\s*")) {
            String errMsg = "Please enter a Definition";
            if (term.matches("\\s*"))
                errMsg = "Please Enter a Term";
            new AlertDialog.Builder(this)
                .setTitle("Invalid input")
                .setMessage(errMsg)
                .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
        } else {
            if (oldTerm != null)
                quizlet.removeTerm(oldTerm);
            quizlet.createTerm(mySet.get_id(), term, definition);
            terms = quizlet.getSetTerms(mySet.get_id());
            term_view.setAdapter(new TermListAdapter(this, R.layout.set_list_item, terms));
            return true;
        }
        return false;
    }

    /**
     * a List adapter that uses closures to allow multiple views to be clickable
     * while referencing the item they hold
     */
    private class TermListAdapter extends ArrayAdapter<Term> {
        public TermListAdapter(Context context, int resource, List<Term> terms) {
            super(context, resource, terms);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            final Term myTerm = getItem(position);
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.set_list_item, parent, false);
            View text_section = myView.findViewById(R.id.text_section);
            TextView title = (TextView) myView.findViewById(R.id.title_tv);
            TextView description = (TextView) myView.findViewById(R.id.description_tv);
            ImageButton btn = (ImageButton) myView.findViewById(R.id.remove_button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    quizlet.removeTerm(myTerm);
                    terms = quizlet.getSetTerms(mySet.get_id());
                    term_view.setAdapter(new TermListAdapter(SetActivity.this, R.layout.set_list_item, terms));
                }
            });
            text_section.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new TermDialog(myTerm).show();
                }
            });
            title.setText(myTerm.getTerm());
            description.setText(myTerm.getDefinition());
            return myView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.study:
                study();
                return true;
            case R.id.new_term:
                new TermDialog().show();
                return true;
            case android.R.id.home:
                if (terms.isEmpty()) {
                    Intent intent = new Intent(this, SetListActivity.class);
                    startActivity(intent);
                    return true;
                } else {
                    study();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * attempt to start the study activity for this set, tell the user
     * why they are unable to if the set is empty
     */
    public void study(){
        if (mySet != null && !terms.isEmpty()) {
            Intent intent = new Intent(this, Study.class);
            intent.putExtra("set", mySet);
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(this, R.string.no_terms_warning, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
