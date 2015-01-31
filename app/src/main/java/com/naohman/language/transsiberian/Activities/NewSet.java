package com.naohman.language.transsiberian.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.naohman.language.transsiberian.Helpers.QuizletSet;
import com.naohman.language.transsiberian.R;
import com.naohman.language.transsiberian.Singletons.Quizlet;


public class NewSet extends ActionBarActivity {
    private RadioGroup term_lang;
    private EditText set_name;
    private EditText set_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_set);
        term_lang = (RadioGroup) findViewById(R.id.term_lang);
        set_name = (EditText) findViewById(R.id.set_name);
        set_description = (EditText) findViewById(R.id.set_description);
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
            if (term_lang.getCheckedRadioButtonId() == R.id.russian_term){
                set = quizlet.createSet(name, description, Quizlet.RUSSIAN);
            } else {
                set = quizlet.createSet(name, description, Quizlet.ENGLISH);
            }
            Intent intent = new Intent(this, SetActivity.class);
            intent.putExtra("set", set);
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_set, menu);
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
}
