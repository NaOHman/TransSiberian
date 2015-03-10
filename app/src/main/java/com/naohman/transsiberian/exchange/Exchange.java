package com.naohman.transsiberian.exchange;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.naohman.language.transsiberian.R;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Jeffrey Lyman
 * An activity that performs currency exchanges
 */
public class Exchange extends ActionBarActivity implements TextView.OnEditorActionListener {
    private final static String CURRENCTY_URL = "http://www.freecurrencyconverterapi.com/api/v3/convert?q=";
    private final static String EURO = "EUR";
    private final static String RUBLE = "RUB";
    private final static String DOLLAR = "USD";
    private final static String YEN = "JPY";
    private final static String POUND = "GBP";
    private final static String SEP = "_";
    private final static String LAST_CURRENCY = "last currency";
    private static final int ANIM_DURATION = 500;
    private static long hour = 3600000;
    private static long day = 86400000;

    private boolean swapped = false;
    private float animDistance, animHeight;
    private float[] rates = new float[2];
    private QuadraticAnimation toRight, toLeft;
    private Menu menu;
    private int whichRate = 0;
    private EditText currency_et;
    private SharedPreferences prefs;
    private String currentCurrency;
    private TextView foreign, currency_tv, ruble;
    private LinearLayout tv_container;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        currency_et = (EditText) findViewById(R.id.currency_et);
        currency_et.setOnEditorActionListener(this);
        foreign = (TextView) findViewById(R.id.input_label);
        ruble = (TextView) findViewById(R.id.output_label);
        currency_tv = (TextView) findViewById(R.id.currency_tv);
        tv_container = (LinearLayout) findViewById(R.id.tv_container);
        loading = (ProgressBar) findViewById(R.id.loading);
        prefs = this.getPreferences(Context.MODE_PRIVATE);
        currentCurrency = prefs.getString(LAST_CURRENCY, DOLLAR);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getExchange(false);

        //Set up Animations
        toRight = new QuadraticAnimation();
        toRight.setDuration(ANIM_DURATION);
        toRight.setFillAfter(true);
        toLeft = new QuadraticAnimation();
        toLeft.setDuration(ANIM_DURATION);
        toLeft.setFillAfter(true);
        ViewTreeObserver vto = foreign.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                animDistance = foreign.getX() - ruble.getX();
                animHeight = foreign.getHeight() / 2;
            }
        });
    }

    /**
     * a function that converts between the input and output currencies
     */
    public void convert(View v){
        String base = currency_et.getText().toString();
        if (rates[whichRate] == 0.0 || base.matches("\\s*")){
            currency_tv.setText("");
        } else {
            float baseRate  = Float.parseFloat(base);
            if (swapped ^ whichRate == 1){
                currency_tv.setText("" + (baseRate / rates[whichRate]));
            } else {
                currency_tv.setText("" + (baseRate * rates[whichRate]));
            }
        }
    }

    /**
     * Get the exchange rate for the current currency
     * @param force whether to force an updayed
     */
    public void getExchange(final boolean force){
        currency_et.setText("");
        currency_tv.setText("");
        long elapsed = System.currentTimeMillis() - prefs.getLong(currentCurrency,0);
        rates[0] = prefs.getFloat(currentCurrency+SEP+RUBLE, 0f);
        rates[1] = prefs.getFloat(RUBLE+SEP+currentCurrency, 0f);

        //If the rate is less three hours old, don't bother updating it
        if (elapsed < 3*hour && !force)
            return;

        //make sure we're connected before trying to pull data
        ConnectivityManager cManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            new ExchangeFetcher().execute();
        } else {
            //If the exchange rate is more than a day old, warn the user
            if (elapsed != 0) {
                String days = "" + ((int) elapsed / day);
                String plural = elapsed > day * 2 ? "s": "";
                new AlertDialog.Builder(this)
                        .setTitle(R.string.warning)
                        .setMessage(String.format(getString(R.string.old_currency), days, plural))
                        .setPositiveButton(R.string.use_old, null)
                        .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getExchange(force);
                            }
                        })
                        .show();
            } else {
                //If there is no currency disable exchange and alert the user
                tv_container.setVisibility(View.INVISIBLE);
                new AlertDialog.Builder(this)
                        .setMessage(R.string.no_internet)
                        .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getExchange(force);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exchange, menu);
        this.menu = menu;
        updateCurrency(currentCurrency);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_usd:
                updateCurrency(DOLLAR);
                return true;
            case R.id.action_eur:
                updateCurrency(EURO);
                return true;
            case R.id.action_gbp:
                updateCurrency(POUND);
                return true;
            case R.id.action_jpy:
                updateCurrency(YEN);
                return true;
            case R.id.refresh:
                getExchange(true);
            case R.id.which_rate:
                item.setChecked(!item.isChecked());
                whichRate = item.isChecked() ? 0 : 1;
                convert(null);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set the currency and change the menu to reflect the new currency
     * @param newCurrency the new currency
     */
    private void updateCurrency(String newCurrency){
        currentCurrency = newCurrency;
        MenuItem currency = menu.findItem(R.id.currency);
        if (currentCurrency.equalsIgnoreCase(DOLLAR)) {
            currency.setIcon(R.drawable.usd);
            foreign.setText(R.string.usd);
        } else if (currentCurrency.equalsIgnoreCase(EURO)) {
            currency.setIcon(R.drawable.eur);
            foreign.setText(R.string.eur);
        } else if (currentCurrency.equalsIgnoreCase(POUND)) {
            currency.setIcon(R.drawable.gbp);
            foreign.setText(R.string.gbp);
        } else if (currentCurrency.equalsIgnoreCase(YEN)) {
            currency.setIcon(R.drawable.jpy);
            foreign.setText(R.string.jpy);
        }
        getExchange(false);
    }

    /**
     * a function that switches the input currency and ouput currency
     * now with animations!
     */
    public void swap(View v){
        toRight.setPath(-animDistance, -animHeight, swapped);
        toLeft.setPath(animDistance, animHeight, swapped);
        foreign.startAnimation(toRight);
        ruble.startAnimation(toLeft);
        swapped = !swapped;
        convert(null);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO){
            convert(null);
            return true;
        }
        return false;
    }


    /**
     * A private class that attempts to pull currency exchange data from the internet
     */
    private class ExchangeFetcher extends AsyncTask<Void,Void,float[]>{
        @Override
        protected void onPreExecute(){
            tv_container.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected float[] doInBackground(Void... params) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                float[] rates = new float[2];
                rates[0] = fetchRate(httpClient, currentCurrency + SEP + RUBLE);
                rates[1] = fetchRate(httpClient, RUBLE + SEP + currentCurrency);
                return rates;
            } catch (Exception e) {
                Log.e("Error fetching currency", e.getMessage());
            }
            return new float[0];
        }

        @Override
        protected void onPostExecute(float[] newRates) {
            tv_container.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
            //This probably means something out of the user's control happened
            if (newRates.length == 0){
                new AlertDialog.Builder(Exchange.this)
                        .setMessage(R.string.fetch_error)
                        .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getExchange(false);
                            }
                        }).setNegativeButton(R.string.cancel, null)
                        .show();
                //no currency data, disable exchange
                if (rates[0] == 0)
                    tv_container.setVisibility(View.INVISIBLE);
            } else {
                Exchange.this.rates = newRates;
                //cache the data for future use
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat(currentCurrency+SEP+RUBLE, rates[0]);
                editor.putFloat(RUBLE+SEP+currentCurrency, rates[1]);
                editor.putLong(currentCurrency, System.currentTimeMillis());
                editor.apply();
            }
        }
    }

    /**
     * Pull and exchange rate from the internet
     * @param client the client used to make the request
     * @param query a query representing the desired exchange rate
     * @return a float representing the exchange rate
     * @throws Exception that may have occured so that the async task can handle it
     */
    private static float fetchRate(HttpClient client, String query) throws Exception {
        try {
            URI address = new URI(CURRENCTY_URL + query);
            HttpGet request = new HttpGet();
            request.setURI(address);
            HttpResponse res = client.execute(request);
            InputStream inStream = res.getEntity().getContent();
            String json = IOUtils.toString(inStream);
            JSONObject jObj = new JSONObject(json);
            return (float) jObj.getJSONObject("results")
                    .getJSONObject(query)
                    .getDouble("val");
        } catch (URISyntaxException e) {
            Log.e("Bad URI Syntax", e.getMessage());
            return 0;
        }
    }
}
