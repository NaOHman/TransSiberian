package com.naohman.language.transsiberian;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.util.Stack;

public class Translate extends ActionBarActivity implements
        TextToSpeech.OnInitListener, View.OnClickListener, SpanListener{
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
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        new SetUpTask().execute();
    }

    public void initLayout(){
        setContentView(R.layout.activity_translate);
        getSupportActionBar().show();
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
        private TextView tv_message;
        private ProgressBar pb_loading;
        @Override
        protected void onPreExecute(){
            setContentView(R.layout.splash);
            getSupportActionBar().hide();
            tv_message = (TextView) findViewById(R.id.load_message);
            pb_loading = (ProgressBar) findViewById(R.id.loading);
            pb_loading.setIndeterminate(true);
            pb_loading.setVisibility(View.VISIBLE);
            tv_message.setText("Drinking with Gogol...");
        }
        @Override
        protected Void doInBackground(Void... params) {
            ts = TranslationService.getInstance();
            publishProgress("Consulting Tolstoy...");
            ts.initDB(Translate.this);
            publishProgress("Dueling with Pushkin");
            ts.open();
            publishProgress("Arguing with Turgenyev...");
            tts = new TextToSpeech(Translate.this, Translate.this);
            return null;
        }
        @Override
        protected void onProgressUpdate(String... strings){
            tv_message.setText(strings[0]);
        }
        @Override
        protected void onPostExecute(Void v){
            Translate.this.initLayout();
        }
    }

    public void setTranslation(String keyword) {
        try {
            Spannable translations = ts.getTranslations(keyword, new DictTagHandler(this));
            if (translations == null || translations.length() == 0){
                setTranslation((Spannable) null);
                return;
            }
            setTranslation(translations);
        } catch (IllegalAccessError e) {
            apologize();
        }
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
            if (isTts) {
                say(word);
            } else {
                Toast t = Toast.makeText(Translate.this, "Text To Speech is unavailable", Toast.LENGTH_LONG);
                t.show();
            }
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

    public void apologize(){
        AlertDialog aDial = new AlertDialog.Builder(this).create();
        aDial.setCancelable(true);
        aDial.setTitle("Sorry, I'm not actually ready yet");
        aDial.setMessage("The word you entered isn't in the dictionary. "
                + "Normally we'd try to convert it to dictionary form first "
                + "but that feature hasn't loaded yet. You can try again in a few" +
                " seconds, or try putting it into dictionary form on your own");
        aDial.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        aDial.setButton(DialogInterface.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Translate.this.onClick(Translate.this.btn_translate);
            }
        });
        aDial.show();
    }
}
