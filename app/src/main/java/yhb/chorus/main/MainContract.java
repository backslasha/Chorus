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

        void invalidateWidgets(int progress, int playMode, Bitmap cover, String songName, String artistName);

        void invalidateSeekBarVolumeSystem(int currentVolume, int volumeSystemMax);
    }

    interface Presenter extends BasePresenter {

        void loadQueueMP3sFromDB(PlayCenter playCenter);

        /**
         *
         * @param playCenter
         */
        void loadMP3sFromDB(PlayCenter playCenter);

        void saveCurrentVolume(float volume);

        void next();

        void previous();

        void playOrPause();

        void nextPlayMode();

        void setCurrentVolumeSystem(int volume);

        void reloadCurrentWidgetsData(boolean needCover);

        List<MP3> loadQueueMP3sFromMemory();

        MP3 getCurrentMP3();

        void loadSavedSetting();

        void release();

        int getCurrentVolumeSystem();

        int getMaxVolumeSystem();

        void point(MP3 mp3);
    }
}
