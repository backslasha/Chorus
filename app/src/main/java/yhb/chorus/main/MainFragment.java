package yhb.chorus.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import yhb.chorus.common.SimpleAdapter;
import yhb.chorus.common.SimpleHolder;
import yhb.chorus.entity.MP3;
import yhb.chorus.list.ListActivity;
import yhb.chorus.service.MainService;
import yhb.chorus.utils.ActivityUtils;

import static yhb.chorus.service.PlayCenter.MODE_LIST_LOOP;
import static yhb.chorus.service.PlayCenter.MODE_RANDOM;
import static yhb.chorus.service.PlayCenter.MODE_SINGLE_LOOP;

/**
 * Created by yhb on 18-1-17.
 */

public class MainFragment extends Fragment implements MainContract.View, View.OnClickListener {

    private MainContract.Presenter mPresenter;
    private ImageView mImageViewCover;
    private ImageButton buttonPlayOrPause;
    private ImageButton buttonPlayMode;
    private TextView textViewCurrentProgress, textViewMaxProgress, textViewSongName, textViewArtistName;
    private SeekBar mSeekBar, mSeekBarVolume, mSeekBarVolumeSystem;
    private ConsoleReceiver mReceiver;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mReceiver = new ConsoleReceiver();
        IntentFilter intentFilter = new IntentFilter(MainService.ACTION_RENEW_PROGRESS);
        intentFilter.addAction(MainService.ACTION_CHANGE_FINISH);
        getActivity().registerReceiver(mReceiver, intentFilter);
        super.onCreate(savedInstanceState);
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
        mPresenter.loadSavedSetting();
        mPresenter.reloadCurrentWidgetsData(true);
        invalidateSeekBarVolumeSystem(
                mPresenter.getCurrentVolumeSystem(),
                mPresenter.getMaxVolumeSystem()
        );
    }

    private void bindViews(View root) {
        ImageButton buttonNext = root.findViewById(R.id.image_button_next);
        ImageButton buttonPrevious = root.findViewById(R.id.image_button_previous);
        buttonPlayMode = root.findViewById(R.id.image_button_play_mode);
        ImageButton buttonQueueMusic = root.findViewById(R.id.image_button_queue_music);
        buttonPlayOrPause = root.findViewById(R.id.image_button_play_or_pause);
        mImageViewCover = root.findViewById(R.id.image_view_cover);
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
                    Intent proChangeIntent = new Intent(MainService.ACTION_PROGRESS_CHANGE);
                    proChangeIntent.putExtra("changeTo", progress);
                    getActivity().sendBroadcast(proChangeIntent);
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
                    Intent proChangeIntent = new Intent(MainService.ACTION_SET_VOLUME);
                    proChangeIntent.putExtra("progress", progress);
                    getActivity().sendBroadcast(proChangeIntent);

                    float volume = ((float) progress) / ((float) seekBar.getMax());
                    mPresenter.saveCurrentVolume(volume);
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
                    mPresenter.setCurrentVolumeSystem(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int screenWidth = ActivityUtils.getScreenWidth(getActivity());
        ViewGroup.LayoutParams layoutParams = mImageViewCover.getLayoutParams();
        layoutParams.width = screenWidth * 4 / 5;
        layoutParams.height = layoutParams.width;
        mImageViewCover.setLayoutParams(layoutParams);

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
        getActivity().unregisterReceiver(mReceiver);
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
                break;
            case R.id.image_button_play_or_pause:
                mPresenter.playOrPause();
                mPresenter.reloadCurrentWidgetsData(true);
                break;
            case R.id.image_button_previous:
                mPresenter.previous();
                break;
            case R.id.image_button_play_mode:
                mPresenter.nextPlayMode();
                mPresenter.reloadCurrentWidgetsData(false);
                break;
            case R.id.image_button_queue_music:
                showBottomSheet();
                break;
        }
    }

    @Override
    public void invalidateWidgets(int progress, int playMode, Bitmap cover, String songName, String artistName) {

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

        if (cover != null) {
            mImageViewCover.setImageBitmap(cover);
        }

        textViewSongName.setText(songName);

        textViewArtistName.setText(artistName);
    }

    @Override
    public void invalidateSeekBarVolumeSystem(int currentVolume, int volumeSystemMax) {
        mSeekBarVolumeSystem.setMax(volumeSystemMax);
        mSeekBarVolumeSystem.setProgress(currentVolume);
    }

    private BottomSheetDialog bottomSheetDialog = null;

    private SimpleAdapter<MP3> mMP3SimpleAdapter;

    private void showBottomSheet() {
        if (bottomSheetDialog != null && mMP3SimpleAdapter != null) {
            mMP3SimpleAdapter.performDataChanged(mPresenter.loadQueueMP3s());
            bottomSheetDialog.show();
            return;
        }
        mMP3SimpleAdapter = new SimpleAdapter<MP3>(getActivity(), R.layout.item_mp3_simple) {
            @Override
            public void forEachHolder(SimpleHolder holder, final MP3 mp3) {
                TextView textView = holder.getView(R.id.text_view_song_name);
                textView.setText(String.format("%s / %s", mp3.getTitle(), mp3.getArtist()));
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       mPresenter.point(mp3);
                       mPresenter.reloadCurrentWidgetsData(true);
                    }
                });
            }
        };
        RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(getActivity())
                .inflate(R.layout.content_queue_song, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mMP3SimpleAdapter);
        mMP3SimpleAdapter.performDataChanged(mPresenter.loadQueueMP3s());

        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        bottomSheetDialog.setContentView(recyclerView);

        // 曲线救国设置 peekHeight（出现时的高度）
        int maxHeight = ActivityUtils.getScreenHeight(getActivity()) * 3 / 5;
        View view = bottomSheetDialog.getWindow().findViewById(android.support.design.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(view).setPeekHeight(maxHeight);

        bottomSheetDialog.show();

        // 设置最大高度
        bottomSheetDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, maxHeight);
        // 解决设置最大高度后的悬空问题
        bottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private class ConsoleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case MainService.ACTION_CHANGE_FINISH:
                    mPresenter.reloadCurrentWidgetsData(true);
                    break;
                default:
                    MP3 currentMP3 = mPresenter.getCurrentMP3();
                    if (currentMP3 == null) {
                        return;
                    }

                    int currentProgress = intent.getIntExtra("currentProgress", 0);
                    int maxProgress = currentMP3.getDuration();

                    mSeekBar.setMax(maxProgress);
                    mSeekBar.setProgress(currentProgress);

                    textViewMaxProgress.setText(mm2min(maxProgress));
                    textViewCurrentProgress.setText(mm2min(currentProgress));

                    if (intent.getBooleanExtra("isPlaying", false)) {
                        buttonPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline);
                    } else {
                        buttonPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline);
                    }
                    break;
            }


        }
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
}
