package com.naohman.language.transsiberian.Activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.naohman.language.transsiberian.Helpers.FlashCardView;
import com.naohman.language.transsiberian.Helpers.QuizletSet;
import com.naohman.language.transsiberian.Helpers.Term;
import com.naohman.language.transsiberian.R;
import com.naohman.language.transsiberian.Singletons.Quizlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Study extends ActionBarActivity implements View.OnClickListener, View.OnTouchListener {
    private TextView set_title, set_description;
    private ViewFlipper flashCard;
    private AnimatorSet setRightOut, setLeftIn;
    private float initialX, initialY, minSwipe;
    private List<FlashCardView> terms;
    private int current = 0;
    private int total = 0;
    private List<FlashCardView> saved = new ArrayList<>();
    private QuizletSet mySet;
    private static Animation downIn, lOut, rOut;
    private boolean backFirst = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        flashCard = (ViewFlipper) findViewById(R.id.flashcard);
        mySet = (QuizletSet) getIntent().getSerializableExtra("set");
        set_title = (TextView) findViewById(R.id.set_title);
        set_title.setText(mySet.getTitle());
        set_description = (TextView) findViewById(R.id.set_description);
        set_description.setText(mySet.getDescription());
        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        terms = makeViews(quizlet.getSetTerms(mySet.get_id()));
        current = terms.size();
        total = current;
        minSwipe = flashCard.getWidth() / 3;
        lOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        rOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        downIn = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        setRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_out);
        setLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_in);
        flashCard.setInAnimation(downIn);
        setFlashCards(terms);
    }

    private List<FlashCardView> makeViews(List<Term> terms){
        List<FlashCardView> views = new ArrayList<>();
        for (Term term : terms) {
            FlashCardView v = new FlashCardView(this, null, term.getTerm(), term.getDefinition(), backFirst);
            v.setOnClickListener(this);
            v.setOnTouchListener(this);
            views.add(v);
        }
        return views;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_study, menu);
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
    public void onClick(View v) {
        FlashCardView card = (FlashCardView) v;
        setRightOut.setTarget(card.getVisible());
        setLeftIn.setTarget(card.getHidden());
        setLeftIn.start();
        setRightOut.start();
        card.swap();
        Log.d("Clicked", "View switcher");
    }

    public void reset(){
        LayoutInflater inflater = getLayoutInflater();
        View score = inflater.inflate(R.layout.set_complete, flashCard);
        ((TextView) score.findViewById(R.id.completed_set_name)).setText(mySet.getTitle());
        ((TextView) score.findViewById(R.id.score)).
                setText("Round: " + (total - saved.size()) + "/" + total +
                        "\nTotal: " + (terms.size() - saved.size()) + "/" + terms.size());
        if (saved.size() == 0) {
            score.findViewById(R.id.next_round).setVisibility(View.GONE);
        }
    }

    public void nextRound(View v){
        setFlashCards(saved);
    }

    public void restart(View v) {
        setFlashCards(terms);
    }

    public void setFlashCards(List<FlashCardView> views){
        Collections.shuffle(views);
        current = views.size();
        flashCard.removeAllViews();
        for (FlashCardView v : views){
            v.setVisible(backFirst);
            flashCard.addView(v);
        }
        flashCard.showNext();
        saved = new ArrayList<>();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            initialX = event.getX();
            initialY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float dx = initialX - event.getX();
            float dy = initialY - event.getX();
            if(Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > minSwipe) {
                current--;
                if (current == 0){
                    if (dx < 0)
                        saved.add((FlashCardView) v);
                    flashCard.removeView(v);
                    reset();
                } else {
                    if (dx > 0) {
                        flashCard.setOutAnimation(rOut);
                        flashCard.showNext();
                    } else {
                        flashCard.setOutAnimation(lOut);
                        flashCard.showPrevious();
                        saved.add((FlashCardView) v);
                    }
                    flashCard.removeView(v);
                }
                return true;
            }
        }
        return false;
    }

    public void edit(View v){
        Intent intent = new Intent(this, SetActivity.class);
        intent.putExtra("set", mySet);
        startActivity(intent);
    }
}
