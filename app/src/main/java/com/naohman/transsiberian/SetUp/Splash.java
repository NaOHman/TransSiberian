package com.naohman.transsiberian.SetUp;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import com.naohman.language.transsiberian.R;

/**
 * Created by jeffrey on 1/19/15.
 * Splash screen that shows a train running across the screen
 * Also begins the process of initialization
 */
public class Splash extends Activity implements ViewSwitcher.ViewFactory {
    private ProgressBar pb_loading;
    private ImageSwitcher switcher;
    final int[] images = {R.drawable.car2, R.drawable.car1, R.drawable.car4, R.drawable.car3};
    int imageNumber = 0;
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.splash);
        SetUpManager setUpManager = new SetUpManager();
        setUpManager.loadEngMorphology(getApplicationContext());
        setUpManager.loadRusMorphology(getApplicationContext());
        pb_loading = (ProgressBar) findViewById(R.id.loading);
        switcher= (ImageSwitcher) findViewById(R.id.train_switcher);
        switcher.setFactory(this);
        pb_loading.setIndeterminate(true);
        pb_loading.setVisibility(View.VISIBLE);
        Animation in = AnimationUtils.loadAnimation(this, R.anim.train_in);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.train_out);
        switcher.setInAnimation(in);
        switcher.setOutAnimation(out);
        switcher.setImageResource(R.drawable.color_train);
    }

    /**
     * creates an image view for the image switcher
     * the image view loads the next train car or the landing page when
     * it's animation is completed
     * @return the view
     */
    @Override
    public View makeView() {
        ImageView myView = new ImageView(this) {
            @Override
            public void onAnimationEnd(){
                if (imageNumber >= images.length - 1) {
                    Intent intent = new Intent(Splash.this, Landing.class);
                    startActivity(intent);
                } else {
                    imageNumber++;
                    switcher.setImageResource(images[imageNumber]);
                }
            }
        };
        myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        myView.setLayoutParams(new ImageSwitcher.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT));
        return myView;
    }
}
