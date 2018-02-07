package yhb.chorus.common.adapter;

import android.content.Context;
import android.view.ViewGroup;

import yhb.chorus.common.adapter.base.SimpleHolder;


/**
 * Created by yhb on 17-12-16.
 */

public abstract class MultiItemSimpleAdapter<Entity> extends SimpleAdapter<Entity> {

    public interface MultiItemSupport<Entity> {

        int getItemViewType(int position, Entity entity);

        int getLayoutIdByViewType(int viewType);

    }

    protected MultiItemSupport<Entity> mMultiItemSupport;

    public MultiItemSimpleAdapter(Context context, int layoutId, MultiItemSupport<Entity> multiItemSupport) {
        super(context, layoutId);
        mContext = context;
        mMultiItemSupport = multiItemSupport;
    }

    @Override
    public SimpleHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        return SimpleHolder.createViewHolder(mContext, parent, mMultiItemSupport.getLayoutIdByViewType(viewType));
    }

    @Override
    public int getItemViewType(int position) {
        return mMultiItemSupport.getItemViewType(position, mEntities.get(position));
    }

}