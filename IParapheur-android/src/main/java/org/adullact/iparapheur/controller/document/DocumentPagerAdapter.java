package org.adullact.iparapheur.controller.document;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.artifex.mupdfdemo.MuPDFCore;

import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.PageAnnotations;

public class DocumentPagerAdapter extends FragmentStatePagerAdapter {

	private final SparseArray<PointF> pageSizes = new SparseArray<PointF>();
	private Context context;
	private MuPDFCore muPDFCore;
	private Document document;

	public DocumentPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		this.context = context;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		DocumentPageFragment item = (DocumentPageFragment) super.instantiateItem(container, position);
		if (muPDFCore != null) {
			new DocumentPageLoadingTask((Activity) context, position, item, null, container.getWidth(), container.getHeight()).execute();
		}
		return item;
	}

	@Override
	public Fragment getItem(int i) {
		return new DocumentPageFragment();
	}

    /*@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }*/

	@Override
	public int getCount() {
		return (muPDFCore != null) ? muPDFCore.countPages() : 0;
	}

	@Override
	public String getPageTitle(int position) {
		return "page " + position;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public void setDocument(Document document) throws Exception {
		this.document = document;
		this.pageSizes.clear();
		if (document != null) {
			muPDFCore = new MuPDFCore(context, document.getPath());
		}
		else if (muPDFCore != null) {
			muPDFCore.onDestroy();
			muPDFCore = null;
		}
		notifyDataSetChanged();
	}

	private void updateFragment(int position, Bitmap bm, float scale, DocumentPageFragment fragment, Point initSize) {
		if ((fragment != null) && (fragment.getActivity() != null)) {
			Point pdfSize = null;
			if (pageSizes.get(position) != null)
				pdfSize = new Point(Math.round(pageSizes.get(position).x), Math.round(pageSizes.get(position).y));

			fragment.updatePage(document.getDossierId(), bm, document.getPagesAnnotations().get(position, new PageAnnotations()), initSize, pdfSize);
		}
	}

	private class DocumentPageLoadingTask extends LoadingTask {

		private int mNumPage;
		private int mContainerWidth;
		private int mContainerHeight;
		private float mScale;
		private Point mInitSize;
		private Bitmap mBitmap;
		private DocumentPageFragment mFragment;

		public DocumentPageLoadingTask(Activity context, int numPage, DocumentPageFragment fragment, DataChangeListener listener, int containerWidth, int containerHeight) {
			super(context, listener);
			mNumPage = numPage;
			mContainerWidth = containerWidth;
			mContainerHeight = containerHeight;
			mFragment = fragment;
			mScale = 1f;
		}

		@Override
		protected void load(String... params) throws IParapheurException {
			// Check if this task is cancelled as often as possible.
			if ((muPDFCore == null) || isCancelled())
				return;

			PointF pageSize = pageSizes.get(mNumPage);

			if (pageSize == null) {
				pageSize = muPDFCore.getPageSize(mNumPage);
				pageSizes.put(mNumPage, pageSize);
			}

			if (isCancelled())
				return;

			float initScale = Math.min(mContainerWidth / pageSize.x, mContainerHeight / pageSize.y);
			mInitSize = new Point((int) (pageSize.x * initScale), (int) (pageSize.y * initScale));

			mScale = Math.max(mContainerWidth / pageSize.x, mContainerHeight / pageSize.y);
			Point scaledSize = new Point((int) (pageSize.x * mScale), (int) (pageSize.y * mScale));
			mBitmap = Bitmap.createBitmap(scaledSize.x, scaledSize.y, Bitmap.Config.ARGB_8888);

			if (isCancelled())
				return;

			muPDFCore.drawPage(mBitmap, mNumPage, scaledSize.x, scaledSize.y, 0, 0, scaledSize.x, scaledSize.y);
		}

		@Override
		protected void onPostExecute(String error) {
			super.onPostExecute(error);

			if ((error == null) && (muPDFCore != null))
				updateFragment(mNumPage, mBitmap, mScale, mFragment, mInitSize);
		}
	}
}
