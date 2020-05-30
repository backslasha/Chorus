package yhb.chorus.main;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;

import yhb.chorus.R;
import yhb.chorus.entity.MP3;
import yhb.chorus.entity.MP3InQueue;
import yhb.chorus.service.PlayCenter;

/**
 * Created by yhb on 18-1-17.
 */

public class MainPresenter implements MainContract.Presenter {

    private final ContentObserver mSettingsContentObserver;
    private Context mContext;
    private MainContract.View mView;
    private PlayCenter mPlayCenter;
    private AudioManager mAudioManager;
    private int mVolumeSystem = -1, mVolumeSystemMax = -1;


    public MainPresenter(Context context, MainContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
        mPlayCenter = PlayCenter.getInstance();
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSettingsContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                mVolumeSystem = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mView.invalidateSeekBarVolumeSystem(mVolumeSystem, mVolumeSystemMax);
                super.onChange(selfChange);
            }
        };
        mContext.getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                mSettingsContentObserver
        );
    }

    @Override
    public void start() {
        if (mPlayCenter.getMP3s().size() == 0) {
            loadMP3sFromDBAsync(mPlayCenter);
            loadQueueMP3sFromDBAsync(mPlayCenter);
        }
        mVolumeSystemMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumeSystem = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mView.invalidateSeekBarVolumeSystem(mVolumeSystem, mVolumeSystemMax);
    }

    @Override
    public void release() {
        mContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    /*
     * load and save data from memory/db methods
     */

    @Override
    public void loadQueueMP3sFromDBAsync(final PlayCenter playCenter) {
        DataSupport.findAllAsync(MP3InQueue.class, true).listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<MP3InQueue> mp3InQueue = (List<MP3InQueue>) t;
                ArrayList<MP3> queueMP3s = new ArrayList<>();
                for (MP3InQueue inQueue : mp3InQueue) {
                    queueMP3s.add(inQueue.getMp3());
                }
                playCenter.setQueueMP3s(queueMP3s);
            }
        });
    }

    @Override
    public void loadMP3sFromDBAsync(final PlayCenter playCenter) {
        DataSupport.findAllAsync(MP3.class).listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                playCenter.setMp3s((List<MP3>) t);
            }
        });
    }

    @Override
    public List<MP3> loadQueueMP3sFromMemory() {
        return mPlayCenter.getQueueMP3s();
    }

    @Override
    public MP3 getCurrentMP3() {
        return mPlayCenter.getCurrentMP3();
    }

    /*
     * setting method
     */

    @Override
    public void loadSavedSetting() {
        // todo LoadSaveSetting
    }

    @Override
    public void setVolumeSystem(int volume) {
        mVolumeSystem = volume;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeSystem, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    @Override
    public void reloadCurrentWidgetsData() {

        int progress = (int) (mPlayCenter.getVolumeIndependent() * 10);
        int playMode = mPlayCenter.getPlayMode();
        String songName = "";
        String artistName = "";

        MP3 currentMP3 = mPlayCenter.getCurrentMP3();
        if (currentMP3 != null) {
            songName = currentMP3.getTitle();
            artistName = currentMP3.getArtist();
        } else {
            songName = mContext.getResources().getString(R.string.song_name);
            artistName = mContext.getResources().getString(R.string.artist_name);
        }

        mView.invalidateWidgets(progress, playMode, songName, artistName);
    }

    @Override
    public void loadCoversAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int coverSize = mView.getCoverSize();
                mView.invalidateCovers(mPlayCenter.loadCovers(
                        coverSize, coverSize
                ));
            }
        }).start();
    }

    @Override
    public void reloadConsoleData() {
        mView.invalidatePlayStatus(mPlayCenter.isPlaying(), mPlayCenter.getProgress());
    }

    @Override
    public void setVolume(float volume) {
        mPlayCenter.setVolumeIndependent(volume);
    }

    @Override
    public int getCurrentVolumeSystem() {
        return mVolumeSystem;
    }

    @Override
    public int getMaxVolumeSystem() {
        return mVolumeSystemMax;
    }


    /*
     * music control methods
     */

    @Override
    public void next() {
        mPlayCenter.next(true);
    }

    @Override
    public void playOrPause() {
        mPlayCenter.playOrPause();
    }

    @Override
    public void seekTo(int progress) {
        mPlayCenter.seekTo(progress);
    }

    @Override
    public void previous() {
        mPlayCenter.previous(true);
    }

    @Override
    public void point(MP3 mp3) {
        mPlayCenter.point(mp3);
    }

    @Override
    public void nextPlayMode() {
        mPlayCenter.nextPlayMode();
    }


}
