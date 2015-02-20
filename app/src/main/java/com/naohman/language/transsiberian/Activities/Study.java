package com.naohman.language.transsiberian.Activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.naohman.language.transsiberian.Helpers.FlashCardPair;
import com.naohman.language.transsiberian.Helpers.QuizletSet;
import com.naohman.language.transsiberian.Helpers.Term;
import com.naohman.language.transsiberian.R;
import com.naohman.language.transsiberian.Singletons.Quizlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Study extends ActionBarActivity implements View.OnClickListener, View.OnTouchListener, Animator.AnimatorListener {
    private TextView set_title, set_description;
    private FrameLayout holder;
    private AnimatorSet flipOut, flipIn;
    private float initialX, initialY, minSwipe;
    private List<FlashCardPair> terms;
    private List<FlashCardPair> saved = new ArrayList<>();
    private List<FlashCardPair> active = new ArrayList<>();
    private int current = 0;
    private QuizletSet mySet;
    private static Animation downIn, lOut, rOut;
    private boolean backFirst = false;

    //TODO add prompt with definition
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        holder = (FrameLayout) findViewById(R.id.flashcard_holder);
        mySet = (QuizletSet) getIntent().getSerializableExtra("set");
        set_title = (TextView) findViewById(R.id.set_title);
        set_title.setText(mySet.getTitle());
        set_description = (TextView) findViewById(R.id.set_description);
        set_description.setText(mySet.getDescription());
        Quizlet quizlet = Quizlet.getInstance(getApplicationContext());
        quizlet.open();
        terms = makeFlashCards(quizlet.getSetTerms(mySet.get_id()));
        minSwipe = 80;
//        lOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
//        rOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
//        downIn = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
//        flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_out);
//        flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flip_left_in);
//        flipIn.addListener(this);
        setFlashCards(terms);
    }

    private List<FlashCardPair> makeFlashCards(List<Term> terms){
        List<FlashCardPair> cards = new ArrayList<>();
        for (Term term : terms) {
            FlashCardPair card = new FlashCardPair(this, term.getTerm(), term.getDefinition(), backFirst);
            card.setOnClickListener(this);
            card.setOnTouchListener(this);
            cards.add(card);
        }
        return cards;
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
            backFirst = !backFirst;
            setFlashCards(terms);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        active.get(current).swap();
        holder.removeAllViews();
        holder.addView(active.get(current).getVisible());
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        active.get(current).swap();
        holder.removeView(active.get(current).getVisible());
        holder.addView(active.get(current).getVisible());
    }

    public void reset(){
        holder.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        View score = inflater.inflate(R.layout.set_complete, holder);
        ((TextView) score.findViewById(R.id.completed_set_name)).setText(mySet.getTitle());
        ((TextView) score.findViewById(R.id.score)).
                setText("Round: " + (active.size() - saved.size()) + "/" + active.size() +
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

    public void setFlashCards(List<FlashCardPair> views){
        Collections.shuffle(views);
        current = 0;
        active = new ArrayList<>();
        holder.removeAllViews();
        for (FlashCardPair card : views){
            card.setVisible(backFirst);
            active.add(card);
        }
        holder.addView(active.get(current).getView(backFirst));
        saved = new ArrayList<>();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            initialX = event.getRawX();
            initialY = event.getRawY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE){
            v.setLeft((int) (event.getRawX() - initialX));

        }else if (event.getAction() == MotionEvent.ACTION_UP) {
            float dx = initialX - event.getRawX();
            float dy = initialY - event.getRawX();
            if(Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > minSwipe) {
                if (current == active.size()-1){
                    if (dx < 0)
                        saved.add(active.get(current));
                    holder.removeView(v);
                    reset();
                } else {
                    if (dx > 0) {
                        //Animations here

                    } else {
                        //Animations
                        saved.add(active.get(current));
                    }
                    holder.removeAllViews();
                    current++;
                    holder.addView(active.get(current).getView(backFirst));
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

    @Override
    public void onAnimationStart(Animator animation) {}


    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationRepeat(Animator animation) {}
}
