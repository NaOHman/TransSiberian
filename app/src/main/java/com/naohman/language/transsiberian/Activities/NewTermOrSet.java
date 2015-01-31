package com.naohman.language.transsiberian.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.naohman.language.transsiberian.Helpers.QuizletSet;
import com.naohman.language.transsiberian.R;
import com.naohman.language.transsiberian.Singletons.Quizlet;

import java.util.List;

public class NewTermOrSet extends ActionBarActivity {
    private RadioGroup radioGroup;
    private EditText term, def;
    private List<QuizletSet> sets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_term_or_set);
        radioGroup = (RadioGroup) findViewById(R.id.radio_set_group);
        term = (EditText) findViewById(R.id.enter_term);
        def = (EditText) findViewById(R.id.enter_definition);
        Intent intent = getIntent();
        term.setText(intent.getStringExtra("term"));
        def.setText(intent.getStringExtra("definition"));
        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        sets = quizlet.getAllSets();
        LayoutInflater inflater = getLayoutInflater();
        for (int i=0; i <sets.size(); i++){
            RadioButton rb = (RadioButton) inflater.inflate(R.layout.radio_list_element, radioGroup, false);
            rb.setText(sets.get(i).getTitle());
            rb.setId(i);
            radioGroup.addView(rb);
        }
        quizlet.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_term_or_set, menu);
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

    public void newSet(View v){
        Intent intent = new Intent(this, NewSet.class);
        startActivity(intent);
    }

    public void create(View v){
        //TODO make this safe
        QuizletSet set = sets.get(radioGroup.getCheckedRadioButtonId());
        String t = term.getText().toString();
        String d = def.getText().toString();
        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        quizlet.createTerm(set.get_id(), t, d);
    }
}
