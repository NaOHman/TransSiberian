package com.naohman.transsiberian.setUp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.naohman.transsiberian.exchange.Exchange;
import com.naohman.language.transsiberian.R;
import com.naohman.transsiberian.study.Authenticate;
import com.naohman.transsiberian.translation.activity.Translate;

/**
 * Created by Jeffrey Lyman
 * The landing activity that allows users to pick what they want to do with the app
 */
public class Landing extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
    }

    public void translate(View view){
        Intent intent = new Intent(Landing.this, Translate.class);
        startActivity(intent);
    }

    public void exchange(View view){
        Intent intent = new Intent(Landing.this, Exchange.class);
        startActivity(intent);
    }

    public void study(View view){
        Intent intent = new Intent(Landing.this, Authenticate.class);
        startActivity(intent);
    }
}
