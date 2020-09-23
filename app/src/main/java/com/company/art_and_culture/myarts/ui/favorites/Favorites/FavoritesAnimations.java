package com.company.art_and_culture.myarts.ui.favorites.Favorites;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;

public class FavoritesAnimations {


    public static AnimatorSet scaleUp(ImageButton art_share) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 1.0f;
        float endValue = 1.3f;
        set.setDuration(200).playTogether(
                ObjectAnimator.ofFloat(art_share, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(art_share, View.SCALE_Y, startValue, endValue)

        );
        return set;
    }

    public static AnimatorSet scaleDown(ImageButton art_share) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 1.3f;
        float endValue = 1.0f;
        set.setDuration(200).playTogether(
                ObjectAnimator.ofFloat(art_share, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(art_share, View.SCALE_Y, startValue, endValue)

        );
        return set;
    }








}
