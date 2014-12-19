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

import org.adullact.iparapheur.controller.utils.IParapheurException;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.PageAnnotations;

/**
 * Created by jmaire on 29/01/2014.
 */
public class DocumentPagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private MuPDFCore muPDFCore;
    private final SparseArray<PointF> pageSizes = new SparseArray<PointF>();
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
        return (muPDFCore != null)? muPDFCore.countPages() : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
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

    private void updateFragment(int position, Bitmap bm, float scale, DocumentPageFragment fragment) {
        if ((fragment != null) && (fragment.getActivity() != null)) {
            fragment.updatePage(document.getDossierId(), bm, document.getPagesAnnotations().get(position, new PageAnnotations()));
        }
    }

    private class DocumentPageLoadingTask extends LoadingTask {

        private int numPage;
        private int containerWidth;
        private int containerHeight;
        private float scale;
        private Bitmap bm;
        private DocumentPageFragment fragment;

        public DocumentPageLoadingTask(Activity context, int numPage, DocumentPageFragment fragment, DataChangeListener listener, int containerWidth, int containerHeight) {
            super(context, listener);
            this.numPage = numPage;
            this.containerWidth = containerWidth;
            this.containerHeight = containerHeight;
            this.fragment = fragment;
            scale = 1f;
        }

        @Override
        protected void load(String... params) throws IParapheurException {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return;}
            if (muPDFCore != null) {
                PointF pageSize = pageSizes.get(numPage);
                if (pageSize == null) {
                    pageSize = muPDFCore.getPageSize(numPage);
                    pageSizes.put(numPage, pageSize);
                }
                if (isCancelled()) {return;}
                scale = Math.min(containerWidth/pageSize.x, containerHeight/pageSize.y);
                Point scaledSize = new Point((int)(pageSize.x*scale), (int)(pageSize.y*scale));
                bm = Bitmap.createBitmap(scaledSize.x, scaledSize.y, Bitmap.Config.ARGB_8888);
                if (isCancelled()) {return;}
                muPDFCore.drawPage(bm, numPage, scaledSize.x, scaledSize.y, 0, 0, scaledSize.x, scaledSize.y);
                if (isCancelled()) {return;}
            }
        }

        @Override
        protected void onPostExecute(String error) {
            super.onPostExecute(error);
            if ((error == null) && (muPDFCore != null)) {
                updateFragment(numPage, bm, scale, fragment);
            }
        }
    }
}
