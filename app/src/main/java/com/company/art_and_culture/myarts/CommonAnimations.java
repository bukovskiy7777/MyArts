package com.company.art_and_culture.myarts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.company.art_and_culture.myarts.pojo.Art;

public class CommonAnimations {

    public static AnimatorSet downloadFadeIn(ConstraintLayout download_linear, View add_view, View download_view) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 0.2f;
        float endValue = 1f;
        set.setDuration(500).playTogether(
                ObjectAnimator.ofFloat(download_linear, View.ALPHA, startValue, endValue),
                ObjectAnimator.ofFloat(download_linear, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(download_linear, View.SCALE_Y, startValue, endValue),

                ObjectAnimator.ofFloat(add_view, View.ALPHA, startValue, endValue),
                ObjectAnimator.ofFloat(add_view, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(add_view, View.SCALE_Y, startValue, endValue),

                ObjectAnimator.ofFloat(download_view, View.ALPHA, startValue, endValue),
                ObjectAnimator.ofFloat(download_view, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(download_view, View.SCALE_Y, startValue, endValue)
        );
        set.addListener(getDownloadFadeInListener(download_linear, add_view, download_view));
        return set;
    }

    private static AnimatorListenerAdapter getDownloadFadeInListener(final ConstraintLayout download_linear, final View add_view, final View download_view) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                download_linear.setVisibility(View.VISIBLE);
                add_view.setVisibility(View.VISIBLE);
                download_view.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                download_linear.setVisibility(View.VISIBLE);
                add_view.setVisibility(View.VISIBLE);
                download_view.setVisibility(View.VISIBLE);
            }
        };
    }

    public static AnimatorSet downloadTranslation(View add_view, int targetX, int targetY, ProgressBar download_progress) {
        AnimatorSet set = new AnimatorSet();
        int deltaX = targetX - add_view.getLeft() - add_view.getWidth()/2;
        int deltaY = targetY - add_view.getBottom() - add_view.getHeight()/2;
        set.setDuration(1000).playTogether(
                ObjectAnimator.ofFloat(add_view, View.TRANSLATION_X, deltaX),
                ObjectAnimator.ofFloat(add_view, View.TRANSLATION_Y, deltaY)
        );
        set.addListener(getDownloadTranslationListener(download_progress));
        return set;
    }

    private static AnimatorListenerAdapter getDownloadTranslationListener(final ProgressBar download_progress) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                download_progress.setVisibility(View.VISIBLE);
            }
        };
    }

    public static AnimatorSet downloadFadeOut(ConstraintLayout download_linear, View done_view, int leftInitialMargin, int bottomInitialMargin) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 1f;
        float endValue = 0.0f;
        set.setDuration(1500).playTogether(
                ObjectAnimator.ofFloat(download_linear, View.ALPHA, startValue, endValue),
                ObjectAnimator.ofFloat(download_linear, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(download_linear, View.SCALE_Y, startValue, endValue),

                ObjectAnimator.ofFloat(done_view, View.ALPHA, startValue, endValue),
                ObjectAnimator.ofFloat(done_view, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(done_view, View.SCALE_Y, startValue, endValue)
        );
        set.addListener(getDownloadFadeOutListener(download_linear, done_view, leftInitialMargin, bottomInitialMargin));
        return set;
    }

    private static AnimatorListenerAdapter getDownloadFadeOutListener(final ConstraintLayout download_linear, final View done_view, final int leftInitialMargin, final int bottomInitialMargin) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                download_linear.setVisibility(View.VISIBLE);
                done_view.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                download_linear.setVisibility(View.INVISIBLE);
                done_view.setVisibility(View.INVISIBLE);

                if (leftInitialMargin > 0) {
                    ConstraintLayout.MarginLayoutParams marginLayoutParams = (ConstraintLayout.MarginLayoutParams) download_linear.getLayoutParams();
                    marginLayoutParams.setMargins(leftInitialMargin, 0, 0, bottomInitialMargin);
                    download_linear.setLayoutParams(marginLayoutParams);
                }
            }
        };
    }

    public static AnimatorSet likeFadeIn(ImageButton art_like) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 0.2f;
        float endValue = 1.3f;
        set.setDuration(400).playTogether(
                ObjectAnimator.ofFloat(art_like, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(art_like, View.SCALE_Y, startValue, endValue)
        );
        return set;
    }

    public static AnimatorSet likeScaleDown(ImageButton art_like) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 1.3f;
        float endValue = 1.0f;
        set.setDuration(300).playTogether(
                ObjectAnimator.ofFloat(art_like, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(art_like, View.SCALE_Y, startValue, endValue)
        );
        return set;
    }

    public static AnimatorSet shareScaleUp(ImageButton art_share) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 1.0f;
        float endValue = 1.3f;
        set.setDuration(200).playTogether(
                ObjectAnimator.ofFloat(art_share, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(art_share, View.SCALE_Y, startValue, endValue)
        );
        return set;
    }

    public static AnimatorSet shareScaleDown(ImageButton art_share) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 1.3f;
        float endValue = 1.0f;
        set.setDuration(200).playTogether(
                ObjectAnimator.ofFloat(art_share, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(art_share, View.SCALE_Y, startValue, endValue)
        );
        return set;
    }

    public static AnimatorSet likedLayoutFadeIn(ConstraintLayout liked_layout) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 0.0f;
        float endValue = 1.0f;
        set.setDuration(400).playTogether(
                ObjectAnimator.ofFloat(liked_layout, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(liked_layout, View.SCALE_Y, startValue, endValue)
        );
        return set;
    }

    public static AnimatorSet likedLayoutScaleDown(ConstraintLayout liked_layout) {
        AnimatorSet set = new AnimatorSet();
        float startValue = 1.0f;
        float endValue = 0.0f;
        set.setDuration(400).playTogether(
                ObjectAnimator.ofFloat(liked_layout, View.SCALE_X, startValue, endValue),
                ObjectAnimator.ofFloat(liked_layout, View.SCALE_Y, startValue, endValue)
        );
        return set;
    }

    public static void startLikeAnimations(Art newArt, ImageButton art_like, ConstraintLayout liked_layout, boolean isLikeClicked) {
        if(newArt.getIsLiked()) {
            art_like.setImageResource(R.drawable.ic_favorite_red_100dp);
            art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            AnimatorSet set = new AnimatorSet();
            if(isLikeClicked) {
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) { }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        liked_layout.setVisibility(View.VISIBLE);
                        AnimatorSet set = new AnimatorSet();
                        set.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                AnimatorSet set = new AnimatorSet();
                                set.setStartDelay(3000);
                                set.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) { }
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        liked_layout.setVisibility(View.GONE);
                                    }
                                    @Override
                                    public void onAnimationCancel(Animator animation) { }
                                    @Override
                                    public void onAnimationRepeat(Animator animation) { }
                                });
                                set.play(likedLayoutScaleDown(liked_layout));
                                set.start();
                            }
                            @Override
                            public void onAnimationEnd(Animator animation) { }
                            @Override
                            public void onAnimationCancel(Animator animation) { }
                            @Override
                            public void onAnimationRepeat(Animator animation) { }
                        });
                        set.play(likedLayoutFadeIn(liked_layout));
                        set.start();
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) { }
                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                });
            }
            set.playSequentially(likeFadeIn(art_like), likeScaleDown(art_like));
            set.start();
        } else {
            art_like.setImageResource(R.drawable.ic_favorite_border_black_100dp);
            art_like.setScaleType(ImageView.ScaleType.FIT_CENTER);
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(likeFadeIn(art_like), likeScaleDown(art_like));
            set.start();
        }
    }








}
