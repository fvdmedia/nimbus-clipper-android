package com.fvd.pdf;

import com.fvd.nimbus.R;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;

public class SafeAnimatorInflater
{
	private View mView;

	@SuppressLint("NewApi")
	public SafeAnimatorInflater(Activity activity, int animation, View view)
	{
		AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(activity, R.animator.info);
		mView = view;
		set.setTarget(view);
		set.addListener(new Animator.AnimatorListener() {
			public void onAnimationStart(Animator animation) {
				mView.setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(Animator animation) {
			}

			public void onAnimationEnd(Animator animation) {
				mView.setVisibility(View.INVISIBLE);
			}

			public void onAnimationCancel(Animator animation) {
			}
		});
		set.start();
	}
}
