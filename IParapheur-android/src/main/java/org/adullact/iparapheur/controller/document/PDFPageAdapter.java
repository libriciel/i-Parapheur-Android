package org.adullact.iparapheur.controller.document;

/**
 * Created by jmaire on 02/01/2014.
 * Nearly the same as MuPDFPageAdapter.
 */
public class PDFPageAdapter {}/*extends BaseAdapter {

    private final Context mContext;
    private final Map<Integer,List<Annotation>> mAnnotations;
    private final MuPDFCore mCore;
    private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();
    private PDFPageView pageView;
    private Bitmap mSharedHqBm;

    public PDFPageAdapter(Context c, Map<Integer,List<Annotation>> annotations, String path) throws Exception {
        mContext = c;
        mAnnotations = annotations;
        mCore = new MuPDFCore(c, path);
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

    public void clean() {
        if (pageView != null) {
            pageView.clean();
        }
        //mCore.onDestroy();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            if (mSharedHqBm == null || mSharedHqBm.getWidth() != parent.getWidth() || mSharedHqBm.getHeight() != parent.getHeight())
                mSharedHqBm = Bitmap.createBitmap(parent.getWidth(), parent.getHeight(), Bitmap.Config.ARGB_8888);

            pageView = new PDFPageView(mContext, mCore, new Point(parent.getWidth(), parent.getHeight()), mAnnotations);
        } else {
            pageView = (PDFPageView) convertView;
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
            AsyncTask<Void,Void,PointF> sizingTask = new AsyncTask<Void,Void,PointF>() {
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
                    if (pageView.getPage() == position)
                        pageView.setPage(position, result);
                }
            };

            sizingTask.execute((Void) null);
        }
        return pageView;
    }*/
//}
