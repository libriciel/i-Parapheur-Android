package org.adullact.iparapheur.utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.adullact.iparapheur.R;

public class ViewUtils {

	/**
	 * Swap smoothly between content and spinner, the Google way.
	 * <br/>
	 * Code source : http://developer.android.com/training/animation/crossfade.html
	 *
	 * @param context     needed to load some resources
	 * @param contentView should be invisible on start
	 * @param spinnerView will be set to Visibility.GONE at the end
	 */
	public static void crossfade(@NonNull Context context, @NonNull View contentView, @NonNull final View spinnerView) {

		// Cancelling previous animation (overlapping animations produce chaos, fire, and biblical cataclysms)
		contentView.animate().cancel();
		spinnerView.animate().cancel();

		// System default length
		int mShortAnimationDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

		// Set the content view to 0% opacity but visible, so that it is visible
		// (but fully transparent) during the animation.
		contentView.setAlpha(0f);
		contentView.setVisibility(View.VISIBLE);

		// Animate the content view to 100% opacity, and clear any animation
		// listener set on the view.
		contentView.animate().alpha(1f).setDuration(mShortAnimationDuration).setListener(null);

		// Animate the loading view to 0% opacity. After the animation ends,
		// set its visibility to GONE as an optimization step (it won't
		// participate in layout passes, etc.)
		spinnerView.animate().alpha(0f).setDuration(mShortAnimationDuration).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				spinnerView.setVisibility(View.GONE);
				spinnerView.setAlpha(1f);
			}
		});
	}

	/**
	 * Animate a flip between to views, the Google way.
	 * <br/>
	 * Animators source : http://developer.android.com/training/animation/cardflip.html
	 * Animation code source : http://developer.android.com/guide/topics/graphics/prop-animation.html
	 *
	 * @param context           needed to load some resources
	 * @param outView           the card-front view
	 * @param inView            the card-back view
	 * @param animationListener set on the inView animation
	 */
	public static void flip(@NonNull Context context, @NonNull View outView, @NonNull View inView, @Nullable Animator.AnimatorListener animationListener) {

		outView.setAlpha(1f);
		AnimatorSet outAnim = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out);
		outAnim.setTarget(outView);
		outAnim.start();

		inView.setAlpha(0f);
		AnimatorSet inAnim = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_in);
		inAnim.setTarget(inView);
		inAnim.start();

		if (animationListener != null)
			inAnim.addListener(animationListener);
	}
}