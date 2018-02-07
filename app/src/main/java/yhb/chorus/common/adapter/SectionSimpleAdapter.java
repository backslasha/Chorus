package yhb.chorus.common.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import yhb.chorus.common.adapter.base.SimpleHolder;


/**
 * single type of view plus multiple type of headers\footers
 * never no used
 * todo
 */

public abstract class SectionSimpleAdapter<Entity> extends MultiItemSimpleAdapter<Entity> {

    private static final int TYPE_HEADER = -1;
    private static final int TYPE_ORDINARY = 0;

    public interface SectionSupport<T> {

        int sectionHeaderLayoutId();

        void convertHeader(SimpleHolder headerHolder, int headerIndex);

        String getTitle(T t);
    }


    private LinkedHashMap<String, Integer> mSections;
    private SectionSupport<Entity> mSectionSupport;

    public SectionSimpleAdapter(Context context, int layoutId, SectionSupport<Entity> sectionSupport) {
        super(context, layoutId, null);
        mContext = context;
        mSectionSupport = sectionSupport;
        mSections = new LinkedHashMap<>();
        mMultiItemSupport = new MultiItemSupport<Entity>() {
            @Override
            public int getItemViewType(int position, Entity entity) {
                return mSections.values().contains(position) ? TYPE_HEADER : TYPE_ORDINARY;
            }

            @Override
            public int getLayoutIdByViewType(int viewType) {
                return (viewType == TYPE_HEADER) ? mSectionSupport.sectionHeaderLayoutId() : mLayoutId;
            }

        };
        collectSections();
        registerAdapterDataObserver(observer);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + mSections.size();
    }

    @Override
    public void onBindViewHolder(SimpleHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            mSectionSupport.convertHeader(holder, getHeaderIndex(position));
        } else {
            super.onBindViewHolder(holder, getItemIndex(position));
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        unregisterAdapterDataObserver(observer);
    }

    private int getHeaderIndex(int position) {
        int nSections = 0;

        Set<Map.Entry<String, Integer>> entrySet = mSections.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            if (entry.getValue() < position) {
                nSections++;
            } else {
                break;
            }
        }
        return nSections;
    }

    private int getItemIndex(int position) {
        int nSections = 0;

        Set<Map.Entry<String, Integer>> entrySet = mSections.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            if (entry.getValue() < position) {
                nSections++;
            } else {
                break;
            }
        }
        return position - nSections;
    }

    private void collectSections() {
        int n = mEntities.size();
        int nSections = 0;
        mSections.clear();

        for (int i = 0; i < n; i++) {
            String sectionName = mSectionSupport.getTitle(mEntities.get(i));

            if (!mSections.containsKey(sectionName)) {
                mSections.put(sectionName, i + nSections);
                nSections++;
            }
        }

    }

    private final RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            collectSections();
        }
    };


}