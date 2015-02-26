package com.naohman.transsiberian.Study;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.naohman.transsiberian.Quizlet.QuizletSet;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.Quizlet.Quizlet;


/**
 * an Activity that lets people create a new set
 */
public class NewSet extends ActionBarActivity implements TextView.OnEditorActionListener {
    private RadioGroup term_lang;
    private EditText set_name;
    private EditText set_description;
    private boolean goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_set);
        Intent intent = getIntent();
        goBack = intent.getBooleanExtra("finish", false);
        term_lang = (RadioGroup) findViewById(R.id.term_lang);
        set_name = (EditText) findViewById(R.id.set_name);
        set_description = (EditText) findViewById(R.id.set_description);
        set_description.setOnEditorActionListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void createSet(View v){
        if (set_name.getText().toString().matches("\\s*")){
            new AlertDialog.Builder(this)
                    .setTitle("Invalid input")
                    .setMessage("Must specify set name")
                    .show();
        } else {
            String name = set_name.getText().toString();
            String description = set_description.getText().toString();
            Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
            QuizletSet set;
            if (term_lang.getCheckedRadioButtonId() == R.id.russian_term) {
                set = quizlet.createSet(name, description, Quizlet.RUSSIAN);
            } else {
                set = quizlet.createSet(name, description, Quizlet.ENGLISH);
            }
            if (goBack) {
                finish();
            } else {
                Intent intent = new Intent(this, SetActivity.class);
                intent.putExtra("set", set);
                startActivity(intent);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO){
            createSet(null);
            return true;
        }
        return false;
    }
}