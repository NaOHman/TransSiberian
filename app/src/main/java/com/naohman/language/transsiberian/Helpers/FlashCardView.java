package com.naohman.language.transsiberian.Helpers;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.naohman.language.transsiberian.R;

/**
 * Created by jeffrey on 1/29/15.
 */
public class FlashCardView extends RelativeLayout {
    private boolean isBack;
    private TextView front, back;

    public FlashCardView(Context context, AttributeSet attrs, String frontText, String backText, boolean isBack) {
        super(context, attrs);
        front = new TextView(context);
        front.setBackgroundColor(getResources().getColor(R.color.flashCard));
        front.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f);
        front.setLayoutParams(new ViewGroup.LayoutParams
                (LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        front.setGravity(Gravity.CENTER);
        front.setTypeface(null, Typeface.BOLD);
        front.setText(frontText);

        back = new TextView(context);
        back.setBackgroundColor(getResources().getColor(R.color.flashCard));
        back.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
        back.setLayoutParams(new ViewGroup.LayoutParams
                (LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        back.setGravity(Gravity.CENTER);
        back.setText(backText);
        if (isBack) {
            addView(front);
            addView(back);
        } else {
            addView(back);
            addView(front);
        }
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

    public void setVisible(boolean isBack){
        this.isBack = isBack;
        if (isBack)
            front.bringToFront();
        else
            front.bringToFront();
    }

    public void swap(){
        isBack = !isBack;
        setVisible(isBack);
    }
}
