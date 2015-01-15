package com.naohman.language.transsiberian;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.XMLReader;

import java.util.Locale;
import java.util.Stack;

public class Translate extends ActionBarActivity implements
        TextToSpeech.OnInitListener, Html.TagHandler, View.OnClickListener{
    private EditText et_keyword;
    private Button btn_translate;
    private TextView tv_translation;
    private TranslationService ts;
    private ScrollView sv_translation;
    private Stack<Spannable> previous = new Stack<>();
    private Spannable currentTranslation = null;
    private TextToSpeech tts;
    private boolean isTts = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetUpTask().execute();
        setContentView(R.layout.activity_translate);
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
    private class SetUpTask extends AsyncTask<Void,String,Void> {
        private ProgressDialog d = new ProgressDialog(Translate.this);
        @Override
        protected void onPreExecute(){
            d.setCancelable(false);
            d.setTitle("Loading");
            d.setMessage("Drinking with Pushkin...");
            d.setMax(3);
            d.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            ts = new TranslationService(Translate.this);
            publishProgress("Consulting Tolstoy...");
            ts.initDB();
            publishProgress("Dueling with Gogol");
            ts.open();
            publishProgress("Arguing with Turgenyev...");
            tts = new TextToSpeech(Translate.this, Translate.this);
            return null;
        }
        @Override
        protected void onProgressUpdate(String... strings){
            d.setMessage(strings[0]);
            d.setProgress(d.getProgress() + 1);
        }
        @Override
        protected void onPostExecute(Void v){
            if (d.isShowing())
                d.dismiss();
        }
    }

    public void setTranslation(String keyword) {
        Spannable translations = ts.getTranslations(keyword);
        if (translations == null || translations.length() == 0){
            setTranslation((Spannable) null);
            return;
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
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
        ts.close();
    }

    public void say(String text){
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("ru","RU"));
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "This language is not supported");
            } else {
                Log.d("TTS", "Language is supported");
                tts.setLanguage(new Locale("ru","RU"));
                isTts = true;
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    @Override
    public void handleTag(final boolean opening, final String tag,
                          Editable output, final XMLReader xmlReader){
        int l = output.length();
        if (opening){
           if (tag.equalsIgnoreCase("ex")){
               output.setSpan(new RelativeSizeSpan(0.8f),
                       output.length(), output.length(), Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("kref")){
               output.setSpan(new ReferenceLink(""),l,l, Spannable.SPAN_MARK_MARK);

           } else if (tag.equalsIgnoreCase("k")){
               output.setSpan(new StyleSpan(Typeface.BOLD),l,l,Spannable.SPAN_MARK_MARK);
           } else if (tag.equalsIgnoreCase("dtrn")){
               output.setSpan(new SpeechLink(""), l,l,Spannable.SPAN_MARK_MARK);
           }
        } else {
           if (tag.equalsIgnoreCase("ex")){
               int where = getLast(output, RelativeSizeSpan.class);
               output.setSpan(new ForegroundColorSpan(Color.parseColor("#aaaaaa")),
                       where, l, 0);
               output.setSpan(new RelativeSizeSpan(0.8f), where, l, 0);
           } else if (tag.equalsIgnoreCase("kref")){
               int where = getLast(output, ReferenceLink.class);
               output.setSpan(new ReferenceLink(output.subSequence(where, l)), where, l, 0);
           } else if (tag.equalsIgnoreCase("k")){
               int where = getLast(output, StyleSpan.class);
               output.setSpan(new StyleSpan(Typeface.BOLD), where, l, 0);
               output.setSpan(new RelativeSizeSpan(1.25f), where, l, 0);
               handleTtsSpans(output, where, l);
           } else if (tag.equalsIgnoreCase("dtrn")){
               int where = getLast(output, SpeechLink.class);
               handleTtsSpans(output, where, l);
           }
        }
    }

    public void apologize(){
        AlertDialog aDial = new AlertDialog.Builder(this).create();
        aDial.setCancelable(true);
        aDial.setTitle("Sorry, I'm not actually ready yet");
        aDial.setMessage("The word you entered isn't in the dictionary. "
                + "Normally we'd try to convert it to dictionary form first "
                + "but that feature hasn't loaded yet. You can try again in a few" +
                " seconds, or try putting it into dictionary form on your own");
        aDial.setButton(DialogInterface.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        aDial.show();
    }

    private void handleTtsSpans(Editable text, int start, int end){
        String full = text.toString();
        String target = full.substring(start, end);
        String trimmed = target.replaceAll("\\(([^\\)]+)\\)", "");
        trimmed = trimmed.replaceAll("\\w+\\.","");
        trimmed = trimmed.replaceAll("[a-zA-Z]","");
        String[] defs = trimmed.split(";|,|-");
        for (String def: defs){
            def = def.trim();
            if(TranslationService.isRussian(def) && def.length() > 0){
                int s = full.indexOf(def, start);
                text.setSpan(new SpeechLink(def),s, s+def.length(), 0);
            }
        }
    }

    private int getLast(Editable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if(objs.length == 0) {
            return 0;
        } else {
            for (int i=objs.length; i > 0; i--) {
                if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
                    int j = text.getSpanEnd(objs[i-1]);
                    text.removeSpan(objs[i-1]);
                    if (j < 0)
                        return 0;
                    return j;
                }
            }
            return 0;
        }
    }
    private class ReferenceLink extends ClickableSpan {
        private String link;
        public ReferenceLink(CharSequence link) {
            this.link = link.toString();
        }
        @Override
        public void onClick(View widget) {
            Log.d("Link Clicked", link);
            setTranslation(link);
        }
    }

    private class SpeechLink extends ClickableSpan {
        private String word;
        public SpeechLink(String word) {
            this.word = word;
            Log.d("Made Speech Link", word + word.length());
        }
        @Override
        public void onClick(View widget) {
            if (isTts) {
                say(word);
            } else {
                Toast t = Toast.makeText(Translate.this, "Text To Speech is unavailable", Toast.LENGTH_LONG);
                t.show();
            }
        }

    }
}
