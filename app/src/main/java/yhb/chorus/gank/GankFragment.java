package yhb.chorus.gank;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import yhb.chorus.R;
import yhb.chorus.common.adapter.MultiItemSimpleAdapter;
import yhb.chorus.common.adapter.base.SimpleHolder;
import yhb.chorus.common.adapter.wrapper.LoadMoreWrapper;
import yhb.chorus.entity.response.GanHuo;


/**
 * Created by yhb on 18-1-17.
 */

public class GankFragment extends Fragment implements GankContract.View, View.OnClickListener {

    public static final String TYPE_ALL = "all";
    public static final String TYPE_ANDROID = "Android";
        public static final String TYPE_APP = "App";
    public static final String TYPE_WELFARE = "福利";

    public static final String ARGUE_TYPE = "type";

    private GankContract.Presenter mPresenter;
    private String mType = TYPE_ALL;

    public static GankFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(ARGUE_TYPE,type);
        GankFragment fragment = new GankFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mType = getArguments().getString(ARGUE_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gank, container, false);
        bindViews(root);
        mPresenter.start();
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                break;
        }
        return true;
    }

    private LoadMoreWrapper<GanHuo> mMoreAdapter;

    private void bindViews(View root) {
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(mType);
        }


        RecyclerView recyclerView = root.findViewById(R.id.recycler_view_gank);
        MultiItemSimpleAdapter<GanHuo> multiItemSimpleAdapter = new MultiItemSimpleAdapter<GanHuo>(getActivity(), R.layout.item_ganhuo, new MultiItemSimpleAdapter.MultiItemSupport<GanHuo>() {
            @Override
            public int getItemViewType(int position, GanHuo ganHuo) {
                if (ganHuo.getType().equals("福利")) {
                    return 0;
                }
                return 1;
            }

            @Override
            public int getLayoutIdByViewType(int viewType) {
                return R.layout.item_ganhuo;
            }
        }) {
            @Override
            public void convert(SimpleHolder holder, GanHuo ganHuo) {
                TextView textView = holder.getView(R.id.text_view_gan_huo);
                ImageView imageView = holder.getView(R.id.image_view_gan_huo);

                if (holder.getItemViewType() == 0) {
                    textView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    Picasso.with(getActivity())
                            .load(ganHuo.getUrl())
                            .placeholder(new ColorDrawable(Color.WHITE))
                            .error(new ColorDrawable(Color.DKGRAY))
                            .centerInside()
                            .fit()
                            .into(imageView);
                } else {
                    imageView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(
                            Html.fromHtml("<a href=\""
                                    + ganHuo.getUrl() + "\">"
                                    + ganHuo.getDesc() + "</a>"
                                    + "[" + ganHuo.getWho() + "]")
                    );
                    textView.setMovementMethod(LinkMovementMethod.getInstance());

                }
            }
        };


        mMoreAdapter = new LoadMoreWrapper<>(
                multiItemSimpleAdapter,
                new LoadMoreWrapper.LoadingListener<GanHuo>() {
                    @Override
                    public void onLoading(RecyclerView.Adapter adapter) {
                        mPresenter.loadNewPageAsync();
                    }
                },
                R.layout.include_loading_progress_bar
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mMoreAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setPresenter(GankContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_button_next:
                break;
        }
    }

    @Override
    public void showNewPage(List<GanHuo> ganHuoList) {
        mMoreAdapter.performDataSetAdded(ganHuoList);
    }

    @Override
    public String getType() {
        return mType;
    }
}

