package org.adullact.iparapheur.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.adullact.iparapheur.R;

public class SwipeRefreshListFragment extends ListFragment {

	private SwipeRefreshLayout mSwipeRefreshLayout;

	/**
	 * Utility method to check whether a {@link ListView} can scroll up from it's current position.
	 * Handles platform version differences, providing backwards compatible functionality where
	 * needed.
	 */
	private static boolean canListViewScrollUp(ListView listView) {
		return ViewCompat.canScrollVertically(listView, -1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View listFragmentView = getInitialView(inflater, container, savedInstanceState);

		// Now create a SwipeRefreshLayout to wrap the fragment's content view
		mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(listFragmentView.getContext());

		// Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
		// the SwipeRefreshLayout
		mSwipeRefreshLayout.addView(listFragmentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

		// Make sure that the SwipeRefreshLayout will fill the fragment
		mSwipeRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		mSwipeRefreshLayout.setColorSchemeResources(R.color.secondary_500, R.color.secondary_300, R.color.secondary_700);

		// Now return the SwipeRefreshLayout as this fragment's content view
		return mSwipeRefreshLayout;
	}

	/**
	 * This method can be override by subclasses to use a custom layout for the list view (eg. with
	 * a view used when the list is empty)
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return the view containing at least a listView
	 */
	public View getInitialView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Create the list fragment's content view by calling the super method
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * Set the {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener} to listen for
	 * initiated refreshes.
	 *
	 * @see android.support.v4.widget.SwipeRefreshLayout#setOnRefreshListener(android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener)
	 */
	public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
		mSwipeRefreshLayout.setOnRefreshListener(listener);
	}

	/**
	 * Returns whether the {@link android.support.v4.widget.SwipeRefreshLayout} is currently
	 * refreshing or not.
	 *
	 * @see android.support.v4.widget.SwipeRefreshLayout#isRefreshing()
	 */
	public boolean isRefreshing() {
		return mSwipeRefreshLayout.isRefreshing();
	}

	/**
	 * Set whether the {@link android.support.v4.widget.SwipeRefreshLayout} should be displaying
	 * that it is refreshing or not.
	 *
	 * @see android.support.v4.widget.SwipeRefreshLayout#setRefreshing(boolean)
	 */
	public void setRefreshing(boolean refreshing) {
		mSwipeRefreshLayout.setRefreshing(refreshing);
	}

	/**
	 * @return the fragment's {@link android.support.v4.widget.SwipeRefreshLayout} widget.
	 */
	public SwipeRefreshLayout getSwipeRefreshLayout() {
		return mSwipeRefreshLayout;
	}

	/**
	 * Sub-class of {@link android.support.v4.widget.SwipeRefreshLayout} for use in this
	 * {@link android.support.v4.app.ListFragment}. The reason that this is needed is because
	 * {@link android.support.v4.widget.SwipeRefreshLayout} only supports a single child, which it
	 * expects to be the one which triggers refreshes. In our case the layout's child is the content
	 * view returned from
	 * {@link android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
	 * which is a {@link android.view.ViewGroup}.
	 * <p/>
	 * <p>To enable 'swipe-to-refresh' support via the {@link android.widget.ListView} we need to
	 * override the default behavior and properly signal when a gesture is possible. This is done by
	 * overriding {@link #canChildScrollUp()}.
	 */
	private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

		public ListFragmentSwipeRefreshLayout(Context context) {
			super(context);
		}

		/**
		 * As mentioned above, we need to override this method to properly signal when a
		 * 'swipe-to-refresh' is possible.
		 *
		 * @return true if the {@link android.widget.ListView} is visible and can scroll up.
		 */
		@Override
		public boolean canChildScrollUp() {
			final ListView listView = getListView();
			return ((listView.getVisibility() == VISIBLE) && (canListViewScrollUp(listView)));
		}

	}

}
