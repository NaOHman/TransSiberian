package com.naohman.language.transsiberian.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.naohman.language.transsiberian.Helpers.QuizletSet;
import com.naohman.language.transsiberian.Helpers.Term;
import com.naohman.language.transsiberian.R;
import com.naohman.language.transsiberian.Singletons.Quizlet;

import java.util.List;

public class SetActivity extends ActionBarActivity {
    private QuizletSet mySet;
    private Quizlet quizlet;
    private TextView title_tv, description_tv;
    private ListView term_view;
    private EditText term_et, def_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        mySet = (QuizletSet) getIntent().getSerializableExtra("set");
        title_tv = (TextView) findViewById(R.id.set_title);
        title_tv.setText(mySet.getTitle());
        description_tv = (TextView) findViewById(R.id.set_description);
        description_tv.setText(mySet.getDescription());
        term_et = (EditText) findViewById(R.id.term_term);
        def_et = (EditText) findViewById(R.id.term_definition);
        term_view = (ListView) findViewById(R.id.term_lv);
        quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        List<Term> terms = quizlet.getSetTerms(mySet.get_id());
        term_view.setAdapter(new TermListAdapter(this, R.layout.set_list_item, terms));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void remove(View v){
        Term t = (Term) v.getTag();
        quizlet.removeTerm(t);
        List<Term> terms = quizlet.getSetTerms(mySet.get_id());
        term_view.setAdapter(new TermListAdapter(this, R.layout.set_list_item, terms));
    }

    public void study(View v){
        Intent intent = new Intent(this, Study.class);
        intent.putExtra("set", mySet);
        startActivity(intent);
    }

    public void addTerm(View v){
        String term = term_et.getText().toString();
        String definition = def_et.getText().toString();
        if (term.matches("\\s*")) {
            new AlertDialog.Builder(this)
                .setTitle("Invalid input")
                .setMessage("Must specify term")
                .show();
        }else if (definition.matches("\\s*")){
            new AlertDialog.Builder(this)
                .setTitle("Invalid input")
                .setMessage("Must specify definition")
                .show();
        } else {
            quizlet.createTerm(mySet.get_id(), term, definition);
            List<Term> terms = quizlet.getSetTerms(mySet.get_id());
            term_view.setAdapter(new TermListAdapter(this, R.layout.set_list_item, terms));
            term_et.setText("");
            def_et.setText("");
        }
    }

    private class TermListAdapter extends ArrayAdapter<Term> {
        public TermListAdapter(Context context, int resource) {
            super(context, resource);
        }

        public TermListAdapter(Context context, int resource, List<Term> terms) {
            super(context, resource, terms);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.set_list_item, parent, false);
            TextView title = (TextView) myView.findViewById(R.id.title_tv);
            TextView description = (TextView) myView.findViewById(R.id.description_tv);
            ImageButton btn = (ImageButton) myView.findViewById(R.id.remove_button);
            btn.setTag(getItem(position));
            Term myTerm = getItem(position);
            title.setText(myTerm.getTerm());
            description.setText(myTerm.getDefinition());
            return myView;
        }
    }
}
