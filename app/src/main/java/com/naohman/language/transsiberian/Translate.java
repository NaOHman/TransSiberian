package com.naohman.language.transsiberian;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Stack;

public class Translate extends ActionBarActivity {
    EditText et_keyword;
    Button btn_translate;
    TextView tv_translation;
    TranslationService ts;
    ScrollView sv_translation;
    Stack<Spannable> previous = new Stack<>();
    Spannable currentTranslation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        et_keyword = (EditText) findViewById(R.id.et_keyword);
        btn_translate = (Button) findViewById(R.id.btn_translate);
        sv_translation = (ScrollView) findViewById(R.id.sv_translation);
        tv_translation = (TextView) findViewById(R.id.tv_translation);
        tv_translation.setMovementMethod(LinkMovementMethod.getInstance());
        ts = new TranslationService(this);
        ts.open();
        btn_translate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String keyword = et_keyword.getText().toString();
                setTranslation(keyword);
            }
        });
    }

    public void setTranslation(String keyword) {
        Spannable translations = ts.getTranslations(keyword);
        if (translations == null || translations.length() == 0){
            setTranslation((Spannable) null);
            return;
        }
        ClickableSpan[] spans = translations.getSpans
            (0, translations.length(), ClickableSpan.class);
        for (ClickableSpan s : spans){
            int start = translations.getSpanStart(s);
            int end = translations.getSpanEnd(s);
            CharSequence link = translations.subSequence(start, end);
            ReferenceLink newSpan = new ReferenceLink(link.toString());
            translations.removeSpan(s);
            translations.setSpan(newSpan, start, end, 0);
        }
        setTranslation(translations);
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

    public void onClose(){
        ts.close();
    }

    private class ReferenceLink extends ClickableSpan {
        private String link;
        public ReferenceLink(String link) {
            this.link = link;
        }
        @Override
        public void onClick(View widget) {
            setTranslation(link);
        }
    }

}
