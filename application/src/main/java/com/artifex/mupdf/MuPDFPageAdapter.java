package com.artifex.mupdf;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;
import java.util.Map;
import org.adullact.iparapheur.tab.model.Annotation;
import org.adullact.iparapheur.tab.ui.folder.IParapheurPDFPageView;

public class MuPDFPageAdapter extends BaseAdapter {
	private final Context mContext;
	private final MuPDFCore mCore;
	private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();
        private final Map<Integer, List<Annotation>> mAnnoatations;

	public MuPDFPageAdapter(Context c, MuPDFCore core, Map<Integer, List<Annotation>> annotations) {
		mContext = c;
		mCore = core;
                mAnnoatations = annotations;
	}

	public int getCount() {
		return mCore.countPages();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final IParapheurPDFPageView pageView;
		if (convertView == null) {
			pageView = new IParapheurPDFPageView(mContext, mCore, new Point(parent.getWidth(), parent.getHeight()), mAnnoatations);
		} else {
			pageView = (IParapheurPDFPageView) convertView;
		}

		PointF pageSize = mPageSizes.get(position);
		if (pageSize != null) {
			// We already know the page size. Set it up
			// immediately
			pageView.setPage(position, pageSize);
		} else {
			// Page size as yet unknown. Blank it for now, and
			// start a background task to find the size
			pageView.blank(position);
			SafeAsyncTask<Void,Void,PointF> sizingTask = new SafeAsyncTask<Void,Void,PointF>() {
				@Override
				protected PointF doInBackground(Void... arg0) {
					return mCore.getPageSize(position);
				}

				@Override
				protected void onPostExecute(PointF result) {
					super.onPostExecute(result);
					// We now know the page size
					mPageSizes.put(position, result);
					// Check that this view hasn't been reused for
					// another page since we started
					if (pageView.getPage() == position) {
                                            pageView.setPage(position, result);
                                        }
				}
			};

			sizingTask.safeExecute((Void)null);
		}
		return pageView;
	}
}
