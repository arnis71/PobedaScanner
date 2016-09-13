package ru.arnis.pobedascanner.other;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ru.arnis.pobedascanner.R;

/**
 * Created by arnis on 20/08/16.
 */
public class Utils {

    public static void motionDown(View v){
        v.animate().scaleXBy(-0.3f).scaleYBy(-0.3f).setDuration(150).setInterpolator(new OvershootInterpolator()).start();
    }

    public static void motionUp(View v){
        v.animate().scaleX(1f).scaleY(1f).setDuration(300).setInterpolator(new OvershootInterpolator()).start();
    }

    public static void dimAnim(final View dim){
        dim.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.argb(0,0,0,0),Color.argb(180,0,0,0));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                dim.setBackgroundColor((Integer)animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    public static void undimAnim(final View dim){
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),Color.argb(180,0,0,0), Color.argb(0,0,0,0));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                dim.setBackgroundColor((Integer)animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dim.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();

    }

    public static void fabIconAnim(final ImageView fab, final boolean play) {
        AnimatorSet set = new AnimatorSet();
        PropertyValuesHolder pvhXin = PropertyValuesHolder.ofFloat(View.SCALE_X,0f);
        PropertyValuesHolder pvhYin = PropertyValuesHolder.ofFloat(View.SCALE_Y,0f);
        PropertyValuesHolder pvhXout = PropertyValuesHolder.ofFloat(View.SCALE_X,1f);
        PropertyValuesHolder pvhYout = PropertyValuesHolder.ofFloat(View.SCALE_Y,1f);
        ObjectAnimator animationIn = ObjectAnimator.ofPropertyValuesHolder(fab,pvhXin,pvhYin);
        ObjectAnimator animationOut = ObjectAnimator.ofPropertyValuesHolder(fab,pvhXout,pvhYout);
        ObjectAnimator animationRotIn = ObjectAnimator.ofFloat(fab,View.ROTATION,360f);
        ObjectAnimator animationRotOut = ObjectAnimator.ofFloat(fab,View.ROTATION,-360f);
        animationIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (play)
                    fab.setImageResource(R.drawable.fab_pause);
                else fab.setImageResource(R.drawable.fab_play);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        set.play(animationIn).with(animationRotIn).before(animationOut);
        set.play(animationOut).with(animationRotOut);
        set.start();
    }

    public static String getDateFromMilis(String milis){
        milis+="000";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milis));
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMin = calendar.get(Calendar.MINUTE);
        String min;
        min = Integer.toString(mMin);
        if (mMin<10)
            min="0"+min;
        StringBuilder sb = new StringBuilder();
        String month;
        switch (mMonth){
            case 0: month = " Января ";break;
            case 1: month = " Февраля ";break;
            case 2: month = " Марта ";break;
            case 3: month = " Апреля ";break;
            case 4: month = " Мая ";break;
            case 5: month = " Июня ";break;
            case 6: month = " Июля ";break;
            case 7: month = " Августа ";break;
            case 8: month = " Сентября ";break;
            case 9: month = " Октября ";break;
            case 10: month = " Ноября ";break;
            case 11: month = " Декабря ";break;
            default: month = "";
        }
        sb.append(mHour).append(':').append(min).append(" | ").append(mDay).append(month).append(mYear);
        return sb.toString();
    }
}
