package org.adullact.iparapheur.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

public class ViewUtils {

	private void crossfade(@NonNull Context context, @NonNull View contentView, @NonNull final View spinnerView) {
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
			}
		});
	}
}
