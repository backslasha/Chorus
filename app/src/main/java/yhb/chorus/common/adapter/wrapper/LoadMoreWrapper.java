package yhb.chorus.common.adapter.wrapper;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import yhb.chorus.common.adapter.SimpleAdapter;
import yhb.chorus.common.adapter.base.SimpleHolder;

/**
 * Created by yhb on 18-2-7.
 */

public class LoadMoreWrapper<Entity> extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_LOADING = Integer.MAX_VALUE - 2;
    private SimpleAdapter<Entity> mInnerAdapter;
    private LoadingListener mLoadingListener;
    private int mResId;
    private List<Entity> mEntities;

    public interface LoadingListener<T> {
        void onLoading(RecyclerView.Adapter adapter);
    }

    public LoadMoreWrapper(SimpleAdapter<Entity> innerAdapter, LoadingListener<Entity> loadingListener, int resId) {
        mInnerAdapter = innerAdapter;
        mLoadingListener = loadingListener;
        mResId = resId;
        mEntities = mInnerAdapter.getEntities();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= mInnerAdapter.getItemCount()) {
            return VIEW_TYPE_LOADING;
        }
        return mInnerAdapter.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOADING) {
            return SimpleHolder.createViewHolder(parent.getContext(), parent, mResId);
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            mLoadingListener.onLoading(mInnerAdapter);
            return;
        }
        mInnerAdapter.onBindViewHolder((SimpleHolder) holder, position);
    }

    @Override
    public int getItemCount() {
        return mInnerAdapter.getItemCount() + 1;
    }

    public RecyclerView.Adapter getInnerAdapter() {
        return mInnerAdapter;
    }

    public void performDataSetChanged(List<Entity> entities) {
        if (entities == null) {
            notifyItemRangeChanged(0, mEntities.size());
            return;
        }
        this.mEntities.clear();
        this.mEntities.addAll(entities);
        notifyDataSetChanged();
    }

    public void performDataSetAdded(List<Entity> entities) {
        if (entities == null) {
            notifyItemRangeChanged(0, mEntities.size());
            return;
        }
        this.mEntities.addAll(entities);
        notifyDataSetChanged();
    }

    public void performSingleDataAdded(Entity entity) {
        mEntities.add(entity);
        notifyItemInserted(mEntities.size() - 1);
    }

    public void performSingleDataRemoved(Entity entity) {
        int position = mEntities.indexOf(entity);
        if (position != -1) {
            mEntities.remove(entity);
        }
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mEntities.size() - position - 1);
    }
}
