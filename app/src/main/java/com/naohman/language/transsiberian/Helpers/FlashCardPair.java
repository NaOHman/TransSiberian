package com.naohman.language.transsiberian.Helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.naohman.language.transsiberian.R;

/**
 * Created by jeffrey on 1/29/15.
 */
public class FlashCardPair {
    private boolean isBack;
    private TextView front, back;

    public FlashCardPair(Context context, String frontText, String backText, boolean isBack) {
        front = new TextView(context);
        front.setBackgroundColor(context.getResources().getColor(R.color.flashCard));
        front.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f);
        front.setLayoutParams(new ViewGroup.LayoutParams
                (RadioGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        front.setGravity(Gravity.CENTER);
        front.setTypeface(null, Typeface.BOLD);
        front.setText(frontText);

        back = new TextView(context);
        back.setBackgroundColor(context.getResources().getColor(R.color.flashCard));
        back.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
        back.setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        back.setGravity(Gravity.CENTER);
        back.setText(backText);
        this.isBack = isBack;
    }

    public void setOnClickListener(View.OnClickListener listener){
        front.setOnClickListener(listener);
        back.setOnClickListener(listener);
    }

    public void setOnTouchListener(View.OnTouchListener listener){
        front.setOnTouchListener(listener);
        back.setOnTouchListener(listener);
    }

    public TextView getVisible(){
        if (!isBack)
            return front;
        return back;
    }

    public TextView getHidden(){
        if (!isBack)
            return back;
        return front;
    }

    public TextView getView(boolean backFirst){
        if (backFirst){
            back.setVisibility(View.VISIBLE);
            back.bringToFront();
            Log.d("REturning Side", "Back");
            return back;
        } else {
            front.setVisibility(View.VISIBLE);
            front.bringToFront();
            Log.d("REturning Side", "Front");
            return front;
        }
    }
    public void setVisible(boolean side){
        isBack = side;
    }

    public void swap(){
        isBack = !isBack;
    }
}
