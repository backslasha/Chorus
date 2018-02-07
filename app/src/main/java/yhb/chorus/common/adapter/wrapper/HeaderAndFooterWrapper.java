package yhb.chorus.common.adapter.wrapper;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import yhb.chorus.common.adapter.base.SimpleHolder;


/**
 * never no used
 * todo
 */

public class HeaderAndFooterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MESS_UP_BASE = 1564812;

    /**
     * key as viewType, value as the corresponding specified view
     */
    private SparseArray<View> mHeaderViews, mFooterViews;
    private RecyclerView.Adapter mInnerAdapter;

    public HeaderAndFooterWrapper(RecyclerView.Adapter innerAdapter) {
        mInnerAdapter = innerAdapter;
        mHeaderViews = new SparseArray<>();
        mFooterViews = new SparseArray<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return mHeaderViews.keyAt(position);
        }
        if (isFooterPosition(position)) {
            return mFooterViews.keyAt(position - mHeaderViews.size() - mInnerAdapter.getItemCount());
        }
        return mInnerAdapter.getItemViewType(position - mHeaderViews.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View candidate = mHeaderViews.get(viewType);

        if (null != candidate) {
            return SimpleHolder.createViewHolder(parent.getContext(), candidate);
        }

        candidate = mFooterViews.get(viewType);

        if (null != candidate) {
            return SimpleHolder.createViewHolder(parent.getContext(), candidate);
        }

        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderPosition(position)) {
            return;
        }

        if (isFooterPosition(position)) {
            return;
        }

        mInnerAdapter.onBindViewHolder(holder, position);

    }

    private boolean isFooterPosition(int position) {
        return position >= mHeaderViews.size() + mInnerAdapter.getItemCount();
    }

    private boolean isHeaderPosition(int position) {
        return position < mHeaderViews.size();
    }

    @Override
    public int getItemCount() {
        return mHeaderViews.size() + mInnerAdapter.getItemCount() + mFooterViews.size();
    }


    public void addHeader(View view) {
        mHeaderViews.put(mHeaderViews.size() + MESS_UP_BASE, view);
    }

    public void addFooter(View view) {
        mHeaderViews.put(mHeaderViews.size() + MESS_UP_BASE, view);
    }
}
