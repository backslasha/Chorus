package yhb.chorus.main;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Handler;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import yhb.chorus.R;
import yhb.chorus.db.DBUtils;
import yhb.chorus.entity.MP3;
import yhb.chorus.entity.MP3InQueue;
import yhb.chorus.service.PlayCenter;

/**
 * Created by yhb on 18-1-17.
 */

class MainPresenter implements MainContract.Presenter {
    private final ContentObserver mSettingsContentObserver;
    private Context mContext;
    private MainContract.View mView;
    private PlayCenter mPlayCenter;
    private AudioManager mAudioManager = null;
    private int mVolumeSystem = -1, mVolumeSystemMax = -1;

    MainPresenter(Context context, MainContract.View view) {
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
        loadMP3sFromDB(mPlayCenter);
        loadQueueMP3sFromDB(mPlayCenter);
        mVolumeSystemMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumeSystem = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mView.invalidateSeekBarVolumeSystem(mVolumeSystem, mVolumeSystemMax);
    }

    @Override
    public void release() {
        mContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    /**
     *  load and save data from memory/db methods
     */

    @Override
    public void loadQueueMP3sFromDB(final PlayCenter playCenter) {
        List<MP3InQueue> mp3InQueue = DataSupport.findAll(MP3InQueue.class,true);
        ArrayList<MP3> queueMP3s = new ArrayList<>();
        for (MP3InQueue inQueue : mp3InQueue) {
            queueMP3s.add(inQueue.getMp3());
        }
        playCenter.setQueueMP3s(queueMP3s);
    }

    @Override
    public void loadMP3sFromDB(final PlayCenter playCenter) {
        playCenter.setMp3s(DataSupport.findAll(MP3.class));
    }

    @Override
    public List<MP3> loadQueueMP3sFromMemory() {
        return mPlayCenter.getQueueMP3s();
    }

    @Override
    public MP3 getCurrentMP3() {
        return mPlayCenter.getCurrentMP3();
    }

    /**
     * setting method
     */

    @Override
    public void loadSavedSetting() {
        // todo LoadSaveSetting
    }

    @Override
    public void setCurrentVolumeSystem(int volume) {
        mVolumeSystem = volume;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeSystem, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    @Override
    public void reloadCurrentWidgetsData(boolean needCover) {

        int progress = (int) (mPlayCenter.getVolume() * 10);
        int playMode = mPlayCenter.getPlayMode();
        Bitmap cover = null;
        String songName = "";
        String artistName = "";

        MP3 currentMP3 = mPlayCenter.getCurrentMP3();
        if (currentMP3 != null) {
            if (needCover) {
                cover = mPlayCenter.getAlbumart(currentMP3);
            }
            songName = currentMP3.getTitle();
            artistName = currentMP3.getArtist();
        } else {
            if (needCover) {
                cover = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.marry);
            }
            songName = mContext.getResources().getString(R.string.song_name);
            artistName = mContext.getResources().getString(R.string.artist_name);
        }

        mView.invalidateWidgets(progress, playMode, cover, songName, artistName);
    }

    @Override
    public void saveCurrentVolume(float volume) {
        mPlayCenter.recordVolume(volume);
    }

    @Override
    public int getCurrentVolumeSystem() {
        return mVolumeSystem;
    }

    @Override
    public int getMaxVolumeSystem() {
        return mVolumeSystemMax;
    }


    /**
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
