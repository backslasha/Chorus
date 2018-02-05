package yhb.chorus.main;

import android.graphics.Bitmap;

import java.util.List;

import yhb.chorus.BasePresenter;
import yhb.chorus.BaseView;
import yhb.chorus.entity.MP3;
import yhb.chorus.service.PlayCenter;

/**
 * Created by yhb on 18-1-17.
 */

public interface MainContract {

    interface View extends BaseView<Presenter> {

        void invalidateWidgets(int progress, int playMode, String songName, String artistName);

        void invalidateSeekBarVolumeSystem(int currentVolume, int volumeSystemMax);

        void invalidateCovers(Bitmap[] bitmaps);

        void invalidatePlayStatus(boolean playing, int progress);

        int getCoverSize();
    }

    interface Presenter extends BasePresenter {

        void loadQueueMP3sFromDBAsync(PlayCenter playCenter);

        void loadMP3sFromDBAsync(PlayCenter playCenter);

        void setVolume(float volume);

        void next();

        void previous();

        void playOrPause();

        void seekTo(int progress);

        void nextPlayMode();

        void setVolumeSystem(int volume);

        void reloadCurrentWidgetsData();

        List<MP3> loadQueueMP3sFromMemory();

        MP3 getCurrentMP3();

        void loadSavedSetting();

        void release();

        int getCurrentVolumeSystem();

        int getMaxVolumeSystem();

        void point(MP3 mp3);

        void loadCoversAsync();

        void reloadConsoleData();
    }
}
