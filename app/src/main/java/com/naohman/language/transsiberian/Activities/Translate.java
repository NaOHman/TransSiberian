package com.naohman.language.transsiberian.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.naohman.language.transsiberian.Helpers.DictEntry;
import com.naohman.language.transsiberian.Helpers.DictHeading;
import com.naohman.language.transsiberian.Singletons.DictionaryHandler;
import com.naohman.language.transsiberian.R;
import com.naohman.language.transsiberian.Helpers.SpanListener;

import org.apache.lucene.morphology.english.EnglishLuceneMorphology;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Translate extends ActionBarActivity implements
        View.OnClickListener, SpanListener {
    private EditText et_keyword;
    private Button btn_translate;
    private ListView lv_translation;
    private ScrollView sv_translation;
    private ProgressBar pb_loading;
    private Stack<TranslationListAdapter> previous = new Stack<>();
    private TranslationListAdapter current = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        pb_loading = (ProgressBar) findViewById(R.id.translations_loading);
        pb_loading.setVisibility(View.INVISIBLE);
        et_keyword = (EditText) findViewById(R.id.et_keyword);
        btn_translate = (Button) findViewById(R.id.btn_translate);
        sv_translation = (ScrollView) findViewById(R.id.sv_translation);
        lv_translation = (ListView) findViewById(R.id.lv_translation);
        btn_translate.setOnClickListener(Translate.this);
    }

    @Override
    public void onClick(View v) {
        String keyword = et_keyword.getText().toString();
        makeTranslations(keyword);
    }

    public void makeTranslations(String keyword) {
        new AsyncTask<String,Void,List<DictEntry>>(){
            @Override
            protected void onPreExecute(){
                pb_loading.setVisibility(View.VISIBLE);
                lv_translation.setVisibility(View.INVISIBLE);
            }
            @Override
            protected List<DictEntry> doInBackground(String... params) {
                List<DictEntry> entries;
                DictionaryHandler dictionary = DictionaryHandler.getInstance(getApplicationContext());
                dictionary.open();
                entries = dictionary.getTranslations(params[0], getApplicationContext());
                for (DictEntry entry: entries){
                    entry.setSpanListener(Translate.this);
                }
                return entries;
            }
            @Override
            protected void onPostExecute(List<DictEntry> entries){
                pb_loading.setVisibility(View.INVISIBLE);
                lv_translation.setVisibility(View.VISIBLE);
                if (current != null)
                    previous.push(current);
                setTranslation(entries);
            }
        }.execute(keyword);
    }



    public void setTranslation(List<DictEntry> translations){
        if (translations == null && translations.size() > 0){
            List<DictEntry> entries = new ArrayList();
            entries.add(new DictEntry());
            current = new TranslationListAdapter(this, R.layout.translation_tv, entries);
        } else {
            current = new TranslationListAdapter(this, R.layout.translation_tv, translations);
        }
        lv_translation.setAdapter(current);
        sv_translation.fullScroll(ScrollView.FOCUS_UP);
    }

    @Override
    public void onReferenceClick(View v, DictEntry entry, String word){
        makeTranslations(word);
    }

    @Override
    public void onDefinitionClick(View v, DictEntry entry, String word){
        Intent startDef = new Intent(this, Definition.class);
        startDef.putExtra("keyword", word);
        startDef.putExtra("meanings", new String[] {entry.getKeyword()});
        startActivity(startDef);
    }

    @Override
    public void onKeywordClick(View v, DictEntry entry){
        Intent startDef = new Intent(this, Definition.class);
        startDef.putExtra("keyword", entry.getKeyword());
        startDef.putExtra("meanings", entry.getDefinitions());
        startActivity(startDef);
    }

    @Override
    public void onBackPressed(){
        if (previous.isEmpty()){
            this.finish();
        } else {
            current = previous.pop();
            lv_translation.setAdapter(current);
            sv_translation.fullScroll(ScrollView.FOCUS_UP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_translate, menu);
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

    @Override
    protected void onDestroy() {
        DictionaryHandler.getInstance(null).close();
        super.onDestroy();
    }

    private class TranslationListAdapter extends ArrayAdapter<DictEntry> {
        public TranslationListAdapter(Context context, int resource, List<DictEntry> entries) {
            super(context, resource, entries);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.translation_tv, parent, false);
            TextView translation = (TextView) myView.findViewById(R.id.tv_translation);
            DictEntry entry = getItem(position);
            translation.setText(entry.getSpanned());
            translation.setMovementMethod(LinkMovementMethod.getInstance());
            return myView;
        }
    }
}
