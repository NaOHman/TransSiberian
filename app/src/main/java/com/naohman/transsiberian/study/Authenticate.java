package com.naohman.transsiberian.study;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.naohman.language.transsiberian.R;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Authenticate extends ActionBarActivity implements DialogInterface.OnClickListener {
    private final static String AUTH_URL = "https://api.quizlet.com/oauth/token";
    private final static String CODE_URL = "https://quizlet.com/authorize?response_type=code&client_id=%s&scope=read+write_set+write_group&state=Arkansas";
    private ProgressBar pb_auth;
    private SharedPreferences prefs;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);
        prefs = this.getPreferences(Context.MODE_PRIVATE);
        pb_auth = (ProgressBar) findViewById(R.id.pb_auth);
        pb_auth.setVisibility(View.INVISIBLE);
        if (prefs.contains("quizlet_auth_token")) {
            Log.d("Quizlet Integration", "Success!");
            study();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            pb_auth.setVisibility(View.VISIBLE);
            String myCode = data.getQueryParameter("code");
            Log.d("Intent", myCode);
            new FetchAuthorization().execute(myCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_authenticate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void getAuthCode(View v){
        ConnectivityManager cManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            pb_auth.setVisibility(View.VISIBLE);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(String.format(CODE_URL, getString(R.string.client_id))));
            startActivity(browserIntent);
        } else {
             new AlertDialog.Builder(this)
                .setTitle("Can't connect to the internet")
                .setMessage("Please connect to the internet, or skip quizlet integration. You can connect to quizlet any time in the settings menu")
                .setPositiveButton("Try again", this)
                .setNegativeButton("Cancel", this)
                .show();
        }
    }

    public void skip(View v){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("quizlet_integration", false);
        editor.apply();
        study();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        if (which == AlertDialog.BUTTON_POSITIVE)
            getAuthCode(null);
    }

    private void study(){
        Intent launcher = new Intent(this, SetListActivity.class);
        startActivity(launcher);
    }

    private class FetchAuthorization extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... tokens) {
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("https://api.quizlet.com/oauth/token");

                String auth = Base64.encodeToString((getString(R.string.client_id) + ":" +
                        getString(R.string.secret_key)).getBytes(), Base64.DEFAULT);
                post.setHeader("Authorization", "Base " + auth);
                List<NameValuePair> pairs = new ArrayList<>();
                pairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
                pairs.add(new BasicNameValuePair("code", tokens[0]));
                post.setEntity(new UrlEncodedFormEntity(pairs));
                HttpResponse res = client.execute(post);
                InputStream inStream = res.getEntity().getContent();
                String json = IOUtils.toString(inStream);
                JSONObject jObj = new JSONObject(json);
                return jObj.getString("access_token");

            } catch (Exception e) {}
            return "";
        }

        @Override
        protected void onPostExecute(String token) {
            pb_auth.setVisibility(View.INVISIBLE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("quizlet_auth_token", token);
            editor.apply();
            study();
        }
    }
}
