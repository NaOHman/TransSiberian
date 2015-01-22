package com.naohman.language.transsiberian.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.naohman.language.transsiberian.Helpers.DictHeading;
import com.naohman.language.transsiberian.Helpers.DictTagHandler;
import com.naohman.language.transsiberian.Singletons.DictionaryHandler;
import com.naohman.language.transsiberian.R;
import com.naohman.language.transsiberian.Helpers.SpanListener;

import java.util.List;
import java.util.Stack;

public class Translate extends ActionBarActivity implements
        View.OnClickListener, SpanListener {
    private EditText et_keyword;
    private Button btn_translate;
    private TextView tv_translation;
    private ScrollView sv_translation;
    private ProgressBar pb_loading;
    private Stack<Spannable> previous = new Stack<>();
    private Spannable currentTranslation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        pb_loading = (ProgressBar) findViewById(R.id.translations_loading);
        pb_loading.setVisibility(View.INVISIBLE);
        et_keyword = (EditText) findViewById(R.id.et_keyword);
        btn_translate = (Button) findViewById(R.id.btn_translate);
        sv_translation = (ScrollView) findViewById(R.id.sv_translation);
        tv_translation = (TextView) findViewById(R.id.tv_translation);
        tv_translation.setMovementMethod(LinkMovementMethod.getInstance());
        btn_translate.setOnClickListener(Translate.this);
    }

    @Override
    public void onClick(View v) {
        String keyword = et_keyword.getText().toString();
        setTranslation(keyword);
    }


    private Spannable makeSpan(List<DictHeading> headings){
        SpannableStringBuilder s = new SpannableStringBuilder();
        if (headings == null || headings.size() == 0){
                setTranslation((Spannable) null);
                return s.append("No Translations found");
        }
        for (DictHeading h : headings){
           s.append(h.toSpan(new DictTagHandler(this)))
                   .append('\n');
        }
        return s;
    }

    public void setTranslation(String keyword) {
        new AsyncTask<String,Void,Void>(){
            List<DictHeading> headings;
            @Override
            protected void onPreExecute(){
                pb_loading.setVisibility(View.VISIBLE);
                tv_translation.setVisibility(View.INVISIBLE);
            }
            @Override
            protected Void doInBackground(String... params) {
                DictionaryHandler dictionary = DictionaryHandler.getInstance(null);
                dictionary.open();
                headings = dictionary.getTranslations(params[0]);
                return null;
            }
            @Override
            protected void onPostExecute(Void v){
                pb_loading.setVisibility(View.INVISIBLE);
                tv_translation.setVisibility(View.VISIBLE);
                Spannable s = makeSpan(headings);
                setTranslation(s);
            }
        }.execute(keyword);
    }

    public void setTranslation(Spannable translations){
        if (translations == null){
            tv_translation.setText("No Translations found");
        } else {
            tv_translation.setText(translations);
        }
        sv_translation.fullScroll(ScrollView.FOCUS_UP);
        if (currentTranslation != null)
            previous.push(currentTranslation);
        currentTranslation = translations;
    }

    @Override
    public void onReferenceClick(View v, String word){
        setTranslation(word);
    }

    @Override
    public void onDefinitionClick(View v, String word){
        Intent startDef = new Intent(this, Definition.class);
        startDef.putExtra("keyword",word);
        startActivity(startDef);
    }

    @Override
    public void onBackPressed(){
        if (previous.isEmpty()){
            this.finish();
        } else {
            currentTranslation = null;
            setTranslation(previous.pop());
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
}
