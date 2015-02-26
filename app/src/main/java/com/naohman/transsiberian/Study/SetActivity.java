package com.naohman.transsiberian.Study;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.naohman.transsiberian.Quizlet.QuizletSet;
import com.naohman.transsiberian.Quizlet.Term;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.Quizlet.Quizlet;
import com.naohman.transsiberian.Quizlet.TermFragment;

import java.util.List;

/**
 * An activity that displays information about a set an allows
 * users to edit their terms
 */
public class SetActivity extends ActionBarActivity implements TermFragment.NewTermListener {
    private QuizletSet mySet;
    private Quizlet quizlet;
    private TextView title_tv, description_tv;
    private ListView term_view;
    private List<Term> terms;
    private FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
     * Called by TermFragment when a new term has been created
     * @param term the new term
     * @param definition the new definition
     */
    @Override
    public void addTerm(String term, String definition) {
        quizlet.createTerm(mySet.get_id(), term, definition);
        terms = quizlet.getSetTerms(mySet.get_id());
        term_view.setAdapter(new TermListAdapter(this, R.layout.set_list_item, terms));
    }

    /**
     * Called by TermFragment when a term has been edited
     * @param oldTerm the term to be edited
     * @param term the new term
     * @param definition the new definition
     */
    @Override
    public void editTerm(Term oldTerm, String term, String definition) {
         if (oldTerm != null)
            quizlet.removeTerm(oldTerm);
        quizlet.createTerm(mySet.get_id(), term, definition);
        terms = quizlet.getSetTerms(mySet.get_id());
        term_view.setAdapter(new TermListAdapter(this, R.layout.set_list_item, terms));
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
                    TermFragment tf =  TermFragment.newInstance(myTerm);
                    tf.show(fm, "Edit term fragment");
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
                TermFragment tf = TermFragment.newInstance();
                tf.show(fm, "new term fragment");
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
