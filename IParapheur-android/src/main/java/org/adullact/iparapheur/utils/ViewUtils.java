/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.PopupMenu;

import org.adullact.iparapheur.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class ViewUtils extends coop.adullactprojet.mupdffragment.utils.ViewUtils {

	/**
	 * Animate a flip between two views, the Google way.
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

	/**
	 * Popup menu doesn't show any icons (in Lollipop),
	 * but an hidden method can be called reflectively to force it.
	 * Maybe some day, in a future Android version, it woudn't be necessary.
	 *
	 * That's not very pretty, but it is in a try/catch, so... Why not.
	 */
	public static void setForceShowIcon(@NonNull PopupMenu popupMenu) {
		try {
			Field[] fields = popupMenu.getClass().getDeclaredFields();
			for (Field field : fields) {
				if ("mPopup".equals(field.getName())) {
					field.setAccessible(true);
					Object menuPopupHelper = field.get(popupMenu);
					Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
					Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
					setForceIcons.invoke(menuPopupHelper, true);
					break;
				}
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Hide and show a view, executing Runnable on hide.
	 *
	 * @param contentView should be invisible on start
	 */
	public static void showAfterDelay(@NonNull final View contentView, long delay) {

		// Cancelling previous animation (overlapping animations produce chaos, fire, and biblical cataclysms)
		contentView.animate().cancel();

		contentView.setAlpha(0f);
		contentView.setVisibility(View.VISIBLE);

		// Animate the content view to 100% opacity
		contentView.animate().alpha(1f).setDuration(CONFIG_SHORT_ANIM_TIME).setStartDelay(delay).setListener(null);
	}

	/**
	 * Hide and show a view, executing Runnable on hide.
	 *
	 * @param contentView should be invisible on start
	 */
	public static void hideAfterDelay(@NonNull final View contentView, long delay) {

		// Cancelling previous animation (overlapping animations produce chaos, fire, and biblical cataclysms)
		contentView.animate().cancel();

		contentView.setAlpha(1f);

		// Animate the content view to 0% opacity
		contentView.animate().alpha(0f).setDuration(CONFIG_SHORT_ANIM_TIME).setStartDelay(delay).setListener(new AnimatorListenerAdapter() {

			@Override public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				contentView.setVisibility(View.GONE);
			}
		});
	}
}
