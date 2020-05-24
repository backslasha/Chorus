package yhb.chorus.list;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.app.NavUtils;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yhb.chorus.R;
import yhb.chorus.common.adapter.SimpleAdapter;
import yhb.chorus.common.adapter.base.SimpleHolder;
import yhb.chorus.entity.MP3;
import yhb.chorus.service.PlayCenter;

/**
 * Created by yhb on 18-1-17.
 */

public class ListFragment extends Fragment implements ListContract.View, View.OnClickListener {

    private ListContract.Presenter mPresenter;
    private ContentLoadingProgressBar mSeekBar;
    private SimpleAdapter<MP3> mMP3SimpleAdapter;
    private Button mButtonSelectAll, mButtonDelete, mButtonAddToQueue;
    private ImageButton mImageButtonCancel;
    private LinearLayout mButtonBarBottom, mButtonBarTop;
    private ArrayList<MP3> mSelectedMP3s = new ArrayList<>();

    public boolean isEditable() {
        return mEditable;
    }

    private boolean mEditable = false;
    private List<MP3> mMP3s;
    private Toolbar mToolbar;

    public static ListFragment newInstance() {
        Bundle args = new Bundle();
        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        bindViews(root);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        mToolbar = root.findViewById(R.id.toolbar);
        activity.setSupportActionBar(mToolbar);

        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mPresenter.start();
        return root;
    }

    private void bindViews(View root) {
        mSeekBar = root.findViewById(R.id.seek_bar);
        mButtonBarBottom = root.findViewById(R.id.linear_layout_button_bar_bottom);
        mButtonBarTop = root.findViewById(R.id.linear_layout_button_bar_top);

        mButtonSelectAll = root.findViewById(R.id.button_select_all);
        mImageButtonCancel = root.findViewById(R.id.image_button_cancel);
        mButtonDelete = root.findViewById(R.id.button_delete);
        mButtonAddToQueue = root.findViewById(R.id.button_add_to_song_queue);

        mButtonSelectAll.setOnClickListener(this);
        mButtonDelete.setOnClickListener(this);
        mButtonAddToQueue.setOnClickListener(this);
        mImageButtonCancel.setOnClickListener(this);

        RecyclerView recyclerViewSongs = root.findViewById(R.id.recycler_songs);
        recyclerViewSongs.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMP3SimpleAdapter = new SimpleAdapter<MP3>(getActivity(), R.layout.item_mp3_editable) {

            @Override
            public void convert(SimpleHolder holder, final MP3 mp3) {


                final CheckBox checkBox = holder.getView(R.id.check_box);
                if (mEditable) {
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setOnCheckedChangeListener(null);
                    checkBox.setChecked(mSelectedMP3s.indexOf(mp3) != -1);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                mSelectedMP3s.add(mp3);
                            } else {
                                mSelectedMP3s.remove(mp3);
                            }
                            mToolbar.setTitle("选中 " + mSelectedMP3s.size() + " 条");
                        }
                    });

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkBox.setChecked(!checkBox.isChecked());
                        }
                    });

                } else {
                    checkBox.setVisibility(View.GONE);
                }


                TextView textView = holder.getView(R.id.text_view_song_name);
                textView.setText(String.format("%s / %s", mp3.getTitle(), mp3.getArtist()));
                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        turnOnEditable(true);
                        checkBox.setChecked(true);
                        return true;
                    }
                });
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlayCenter.getInstance().point(mp3);
                    }
                });
                textView.setClickable(!mEditable);
                textView.setLongClickable(!mEditable);


            }
        };
        recyclerViewSongs.setAdapter(mMP3SimpleAdapter);
    }

    @Override
    public void turnOnEditable(final boolean on) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (on) {
                    mButtonBarBottom.setVisibility(View.VISIBLE);
                    mButtonBarTop.setVisibility(View.VISIBLE);
                    mToolbar.setTitle("选中 0 条");
                } else {
                    mToolbar.setTitle(R.string.app_name);
                    mButtonBarBottom.setVisibility(View.GONE);
                    mButtonBarTop.setVisibility(View.GONE);
                }
                mEditable = on;
                mMP3SimpleAdapter.performDataSetChanged(null);
                mSelectedMP3s.clear();
            }
        });
    }

    private void selectAll(boolean selected) {
        mSelectedMP3s.clear();
        if (selected) {
            mSelectedMP3s.addAll(mMP3s);
            mButtonSelectAll.setText("取消全选");
        } else {
            mButtonSelectAll.setText("全选");
        }
        mToolbar.setTitle("选中 " + mSelectedMP3s.size() + " 条");
        mMP3SimpleAdapter.performDataSetChanged(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.release();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                break;
            case R.id.rescan_local_mp3:
                mPresenter.scanMediaStoreAndCreateDB();
                break;
            case R.id.clean_queue_mp3:
                break;
            case R.id.clear_queue_mp3:
                mPresenter.clearQueue();
                break;
            default:

                break;
        }
        return true;
    }

    @Override
    public void setPresenter(ListContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void showProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void showLocalMP3s(final List<MP3> mp3s) {
        this.mMP3s = mp3s;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMP3SimpleAdapter.performDataSetChanged(mp3s);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_delete:
                // TODO: 18-1-19 delete function 
                break;
            case R.id.button_add_to_song_queue:
                mPresenter.savedIntoQueue(mSelectedMP3s);
                break;
            case R.id.button_select_all:
                selectAll("全选".equals(mButtonSelectAll.getText()));
                break;
            case R.id.image_button_cancel:
                turnOnEditable(false);
                break;
        }
    }

}
