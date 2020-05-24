package yhb.chorus.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import yhb.chorus.R;
import yhb.chorus.common.adapter.SimpleAdapter;
import yhb.chorus.common.adapter.base.SimpleHolder;
import yhb.chorus.entity.MP3;
import yhb.chorus.list.ListActivity;
import yhb.chorus.utils.ActivityUtils;

import static yhb.chorus.main.MainActivity.TAG;
import static yhb.chorus.service.PlayCenter.MODE_LIST_LOOP;
import static yhb.chorus.service.PlayCenter.MODE_RANDOM;
import static yhb.chorus.service.PlayCenter.MODE_SINGLE_LOOP;

/**
 * Created by yhb on 18-1-17.
 */

public class MainFragment extends Fragment implements MainContract.View, View.OnClickListener {

    private MainContract.Presenter mPresenter;
    private ViewPager mCoverViewPager;
    private ImageButton buttonPlayOrPause;
    private ImageButton buttonPlayMode;
    private TextView textViewCurrentProgress, textViewMaxProgress, textViewSongName, textViewArtistName;
    private SeekBar mSeekBar, mSeekBarVolume, mSeekBarVolumeSystem;
    private PageSelectedListener mPageSelectedListener;
    private Runnable invalidateConsole;
    private Handler handler;
    private int mCoverSize = -1;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        handler = new Handler();
        invalidateConsole = new Runnable() {
            @Override
            public void run() {
                try {
                    mPresenter.reloadConsoleData();
                    handler.postDelayed(invalidateConsole, 500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mCoverSize = ActivityUtils.getScreenWidth(getActivity()) * 4 / 5;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        bindViews(root);
        mPresenter.start();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.reloadCurrentWidgetsData();
        mPresenter.loadSavedSetting();
        mPresenter.loadCoversAsync();
        invalidateSeekBarVolumeSystem(
                mPresenter.getCurrentVolumeSystem(),
                mPresenter.getMaxVolumeSystem()
        );

        handler.postDelayed(invalidateConsole, 500);

    }

    private void bindViews(View root) {
        ImageButton buttonNext = root.findViewById(R.id.image_button_next);
        ImageButton buttonPrevious = root.findViewById(R.id.image_button_previous);
        buttonPlayMode = root.findViewById(R.id.image_button_play_mode);
        ImageButton buttonQueueMusic = root.findViewById(R.id.image_button_queue_music);
        buttonPlayOrPause = root.findViewById(R.id.image_button_play_or_pause);
        mCoverViewPager = root.findViewById(R.id.view_pager_cover);
        mSeekBar = root.findViewById(R.id.slim_seek_bar);
        mSeekBarVolume = root.findViewById(R.id.slim_seek_bar_volume);
        mSeekBarVolumeSystem = root.findViewById(R.id.slim_seek_bar_volume_system);
        textViewMaxProgress = root.findViewById(R.id.text_view_max_progress);
        textViewCurrentProgress = root.findViewById(R.id.text_view_current_progress);
        textViewSongName = root.findViewById(R.id.text_view_song_name);
        textViewArtistName = root.findViewById(R.id.text_view_artist_name);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress = -1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    this.progress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progress != -1) {
                    mPresenter.seekTo(progress);
                    progress = -1;
                }
            }
        });

        mSeekBarVolume.setMax(10);
        mSeekBarVolume.setProgress(0);
        mSeekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float volume = ((float) progress) / ((float) seekBar.getMax());
                    mPresenter.setVolume(volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarVolumeSystem.setMax(mPresenter.getMaxVolumeSystem());
        mSeekBarVolumeSystem.setProgress(mPresenter.getCurrentVolumeSystem());
        mSeekBarVolumeSystem.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPresenter.setVolumeSystem(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        ViewGroup.LayoutParams layoutParams = mCoverViewPager.getLayoutParams();
        int coverSize = getCoverSize();
        layoutParams.width = coverSize;
        layoutParams.height = coverSize;

        CoverPagerAdapter coverPagerAdapter = new CoverPagerAdapter();
        int initialIndex = coverPagerAdapter.getCount() / 4 * 3 + 1;
        mPageSelectedListener = new PageSelectedListener(
                initialIndex, new OnSelectedListener() {
            @Override
            public void onNext() {
                mPresenter.next();
                mPresenter.reloadCurrentWidgetsData();
                mPresenter.loadCoversAsync();
                Log.d(TAG, "onNext: ");
            }

            @Override
            public void onPrevious() {
                Log.d(TAG, "onPrevious: ");
                mPresenter.previous();
                mPresenter.reloadCurrentWidgetsData();
                mPresenter.loadCoversAsync();
            }
        }
        );
        mCoverViewPager.setLayoutParams(layoutParams);
        mCoverViewPager.setAdapter(coverPagerAdapter);
        mCoverViewPager.setCurrentItem(initialIndex);
        mCoverViewPager.addOnPageChangeListener(mPageSelectedListener);


        buttonNext.setOnClickListener(this);
        buttonPrevious.setOnClickListener(this);
        buttonPlayOrPause.setOnClickListener(this);
        buttonPlayMode.setOnClickListener(this);
        buttonQueueMusic.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.release();
        handler.removeCallbacks(invalidateConsole);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_out:
                Intent intent = ListActivity.newIntent(getActivity());
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_button_next:
                mPresenter.next();
                mPresenter.reloadCurrentWidgetsData();
                mPresenter.loadCoversAsync();
                break;
            case R.id.image_button_play_or_pause:
                mPresenter.playOrPause();
                mPresenter.reloadCurrentWidgetsData();
                mPresenter.loadCoversAsync();
                break;
            case R.id.image_button_previous:
                mPresenter.previous();
                mPresenter.reloadCurrentWidgetsData();
                mPresenter.loadCoversAsync();
                break;
            case R.id.image_button_play_mode:
                mPresenter.nextPlayMode();
                mPresenter.reloadCurrentWidgetsData();
                break;
            case R.id.image_button_queue_music:
                showBottomSheet();
                break;
        }
    }

    @Override
    public void invalidateSeekBarVolumeSystem(int currentVolume, int volumeSystemMax) {
        mSeekBarVolumeSystem.setMax(volumeSystemMax);
        mSeekBarVolumeSystem.setProgress(currentVolume);
    }

    @Override
    public void invalidateCovers(final Bitmap[] bitmaps) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (bitmaps != null) {
                    ImageView[] covers = ((CoverPagerAdapter) mCoverViewPager.getAdapter()).getCovers();

                    int i = mPageSelectedListener.currentIndex();
                    covers[i].setImageBitmap(bitmaps[1]);

                    switch (i) {
                        case 0:
                            covers[2].setImageBitmap(bitmaps[0]);
                            covers[1].setImageBitmap(bitmaps[2]);
                            break;
                        case 1:
                            covers[0].setImageBitmap(bitmaps[0]);
                            covers[2].setImageBitmap(bitmaps[2]);
                            break;
                        case 2:
                            covers[1].setImageBitmap(bitmaps[0]);
                            covers[0].setImageBitmap(bitmaps[2]);
                            break;
                    }

                }
            }
        });
    }

    @Override
    public void invalidateWidgets(int progress, int playMode, String songName, String artistName) {

        mSeekBarVolume.setProgress(progress);

        switch (playMode) {
            case MODE_LIST_LOOP:
                buttonPlayMode.setImageResource(R.drawable.ic_repeat_list);
                break;
            case MODE_RANDOM:
                buttonPlayMode.setImageResource(R.drawable.ic_shuffle);
                break;
            case MODE_SINGLE_LOOP:
                buttonPlayMode.setImageResource(R.drawable.ic_repeat_one);
                break;
            default:
                buttonPlayMode.setImageResource(R.drawable.ic_repeat_list);
                break;
        }

        textViewSongName.setText(songName);

        textViewArtistName.setText(artistName);
    }

    @Override
    public void invalidatePlayStatus(boolean playing, int progress) {
        MP3 currentMP3 = mPresenter.getCurrentMP3();
        if (currentMP3 == null) {
            return;
        }
        int maxProgress = currentMP3.getDuration();

        mSeekBar.setMax(maxProgress);
        mSeekBar.setProgress(progress);

        textViewMaxProgress.setText(mm2min(maxProgress));
        textViewCurrentProgress.setText(mm2min(progress));

        if (playing) {
            buttonPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline);
        } else {
            buttonPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline);
        }

    }

    @Override
    public int getCoverSize() {
        return mCoverSize;
    }

    private BottomSheetDialog bottomSheetDialog = null;

    private SimpleAdapter<MP3> mQueueMP3SimpleAdapter;

    private void showBottomSheet() {
        if (bottomSheetDialog != null && mQueueMP3SimpleAdapter != null) {
            mQueueMP3SimpleAdapter.performDataSetChanged(mPresenter.loadQueueMP3sFromMemory());
            bottomSheetDialog.show();
            return;
        }
        mQueueMP3SimpleAdapter = new SimpleAdapter<MP3>(getActivity(), R.layout.item_mp3_simple) {
            @Override
            public void convert(SimpleHolder holder, final MP3 mp3) {
                TextView textView = holder.getView(R.id.text_view_song_name);
                textView.setText(String.format("%s / %s", mp3.getTitle(), mp3.getArtist()));
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPresenter.point(mp3);
                        mPresenter.reloadCurrentWidgetsData();
                        mPresenter.loadCoversAsync();
                    }
                });
            }
        };
        RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(getActivity())
                .inflate(R.layout.content_queue_song, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mQueueMP3SimpleAdapter);
        mQueueMP3SimpleAdapter.performDataSetChanged(mPresenter.loadQueueMP3sFromMemory());

        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        bottomSheetDialog.setContentView(recyclerView);

        // 曲线救国设置 peekHeight（出现时的高度）
        int maxHeight = ActivityUtils.getScreenHeight(getActivity()) * 3 / 5;
        View view = bottomSheetDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(view).setPeekHeight(maxHeight);

        bottomSheetDialog.show();

        // 设置最大高度
        bottomSheetDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, maxHeight);
        // 解决设置最大高度后的悬空问题
        bottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private String mm2min(int mm) {
        String min, sec;
        if (mm / 1000 / 60 > 9) {
            min = mm / 1000 / 60 + "";
        } else {
            min = "0" + mm / 1000 / 60;
        }

        if (((mm / 1000) % 60) <= 9) {
            sec = "0" + (mm / 1000) % 60;
        } else {
            sec = (mm / 1000) % 60 + "";
        }
        return min + ":" + sec;
    }

    class CoverPagerAdapter extends PagerAdapter {


        private ImageView covers[];

        ImageView[] getCovers() {
            return covers;
        }

        CoverPagerAdapter() {
            covers = new ImageView[3];

            ImageView cover0, cover1, cover2;

            cover0 = new ImageView(MainFragment.this.getActivity());
            cover1 = new ImageView(MainFragment.this.getActivity());
            cover2 = new ImageView(MainFragment.this.getActivity());

            covers[0] = cover0;
            covers[1] = cover1;
            covers[2] = cover2;

            int screenWidth = ActivityUtils.getScreenWidth(getActivity());
            for (ImageView cover : covers) {
                cover.setBackgroundColor(Color.parseColor("#44888888"));
                cover.setPadding(screenWidth / 50, screenWidth / 50, screenWidth / 50, screenWidth / 50);
                cover.setScaleType(ImageView.ScaleType.FIT_XY);
            }


        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = covers[position % 3];
            if (container.equals(view.getParent())) {
                container.removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE / 2;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    interface OnSelectedListener {

        void onNext();

        void onPrevious();

    }

    class PageSelectedListener implements ViewPager.OnPageChangeListener {

        private int oldIndex;

        private OnSelectedListener mOnSelectedListener;

        PageSelectedListener(int initialIndex, OnSelectedListener onSelectedListener) {
            this.oldIndex = initialIndex;
            mOnSelectedListener = onSelectedListener;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == oldIndex + 1) {
                mOnSelectedListener.onNext();
            } else if (position == oldIndex - 1) {
                mOnSelectedListener.onPrevious();
            }

            oldIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        int currentIndex() {
            return oldIndex % 3;
        }

    }

}

