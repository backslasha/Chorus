package yhb.chorus.common.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import yhb.chorus.common.adapter.base.SimpleHolder;


/**
 * Created by yhb on 17-12-16.
 */

public abstract class SimpleAdapter<Entity> extends RecyclerView.Adapter<SimpleHolder> {

    protected int mLayoutId;

    protected Context mContext;

    protected List<Entity> mEntities;

    protected SimpleAdapter(Context context, int layoutId) {
        mContext = context;
        mLayoutId = layoutId;
        try {
            //noinspection unchecked
            mEntities = ArrayList.class.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SimpleHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        return SimpleHolder.createViewHolder(mContext, parent, mLayoutId);
    }

    @Override
    public void onBindViewHolder(SimpleHolder holder, int position) {
        convert(holder, mEntities.get(position));
    }

    @Override
    public int getItemCount() {
        return mEntities.size();
    }

    public abstract void convert(SimpleHolder holder, Entity entity);

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

    public List<Entity> getEntities() {
        return mEntities;
    }
}