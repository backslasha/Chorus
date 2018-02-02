package yhb.chorus.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import yhb.chorus.app.ChorusApplication;
import yhb.chorus.entity.MP3;


public class PlayCenter {
    /*
     * three kinds of play mode
     */
    public static final int MODE_SINGLE_LOOP = 1;
    public static final int MODE_LIST_LOOP = 2;
    public static final int MODE_RANDOM = 3;

    @SuppressLint("StaticFieldLeak")
    private static PlayCenter sPlayCenter;
    private Context mContext;
    private int currentIndex = 0;
    private int playMode = MODE_LIST_LOOP;
    private float mVolume = 1f;

    private ArrayList<MP3> mQueueMP3s = new ArrayList<>();
    private List<MP3> mp3s;
    private MP3 currentMP3;
    private MP3 candidateNextMP3;
    private MP3 candidatePreviousMP3;

    public static PlayCenter getInstance() {
        if (sPlayCenter == null) {
            sPlayCenter = new PlayCenter();
        }
        return sPlayCenter;
    }

    private PlayCenter() {
        mContext = ChorusApplication.getsApplicationContext();
    }

    /*
     * operations of controlling music player
     */

    public void playOrPause() {

        if (currentMP3 == null) {
            next(true);
            return;
        }

        sendCommand(MainService.ACTION_PLAY_PAUSE);

    }

    public void next(boolean fromUser) {

        if (candidateNextMP3 == null) {
            candidateNextMP3 = pickCandidatePrevious(fromUser);
        }

        currentMP3 = candidateNextMP3;

        sendCommand(MainService.ACTION_NEXT);

        candidateNextMP3 = pickCandidateNext(fromUser);
        candidatePreviousMP3 = pickCandidatePrevious(fromUser);

    }

    public void previous(boolean fromUser) {

        if (candidatePreviousMP3 == null) {
            candidatePreviousMP3 = pickCandidatePrevious(fromUser);
        }

        currentMP3 = candidateNextMP3;

        sendCommand(MainService.ACTION_PREVIOUS);

        candidateNextMP3 = pickCandidateNext(fromUser);
        candidatePreviousMP3 = pickCandidatePrevious(fromUser);
    }

    public void point(MP3 mp3) {

        if (!mQueueMP3s.contains(mp3)) {
            mQueueMP3s.add(mp3);
        }

        currentMP3 = mp3;

        sendCommand(MainService.ACTION_POINT);

        candidateNextMP3 = pickCandidateNext(true);
        candidatePreviousMP3 = pickCandidatePrevious(true);
    }

    public void nextPlayMode() {
        switch (playMode) {
            case MODE_LIST_LOOP:
                playMode = MODE_RANDOM;
                break;
            case MODE_RANDOM:
                playMode = MODE_SINGLE_LOOP;
                break;
            case MODE_SINGLE_LOOP:
                playMode = MODE_LIST_LOOP;
                break;
            default:
                playMode = MODE_LIST_LOOP;
                break;
        }
    }

    private MP3 pickCandidatePrevious(boolean fromUser) {

        int currentIndex = mQueueMP3s.indexOf(currentMP3);

        if (playMode == MODE_LIST_LOOP) {
            if (currentIndex > 0) {
                currentIndex -= 1;
            } else {
                currentIndex = mQueueMP3s.size() - 1;
            }
        } else if (playMode == MODE_RANDOM) {
            currentIndex = (int) (mQueueMP3s.size() * Math.random());
        } else if (playMode == MODE_SINGLE_LOOP) {
            if (fromUser) {
                if (currentIndex > 0) {
                    currentIndex -= 1;
                } else {
                    currentIndex = mQueueMP3s.size() - 1;
                }
            }
        }
        if (currentIndex < 0 || currentIndex >= mQueueMP3s.size()) {
            Toast.makeText(mContext, "Queue is empty!", Toast.LENGTH_SHORT).show();
            return null;
        }

        return mQueueMP3s.get(currentIndex);
    }

    private MP3 pickCandidateNext(boolean fromUser) {

        int currentIndex = mQueueMP3s.indexOf(currentMP3);

        if (playMode == PlayCenter.MODE_LIST_LOOP) {
            if (currentIndex + 1 <= mQueueMP3s.size() - 1) {
                currentIndex += 1;
            } else {
                currentIndex = 0;
            }
        } else if (playMode == PlayCenter.MODE_RANDOM) {
            currentIndex = (int) (mQueueMP3s.size() * Math.random());
        } else if (playMode == PlayCenter.MODE_SINGLE_LOOP) {
            if (fromUser) {
                if (currentIndex > 0) {
                    currentIndex += 1;
                } else {
                    currentIndex = 0;
                }
            }
        }

        if (currentIndex < 0 || currentIndex >= mQueueMP3s.size()) {
            Toast.makeText(mContext, "Queue is empty!", Toast.LENGTH_SHORT).show();
            return null;
        }

        return mQueueMP3s.get(currentIndex);
    }

    private void sendCommand(String action) {

        sureServiceAlive();

        if (currentMP3 != null) {
            Intent intent = new Intent(action);
            mContext.sendBroadcast(intent);
        }
    }

    private void sureServiceAlive() {
        Intent serIntent = new Intent(mContext, MainService.class);
        mContext.startService(serIntent);
    }

    /*
     * global public data get/set methods
     */

    /**
     * 设置当前的本地列表 mp3
     *
     * @param mp3s 本地列表 mp3，一般从数据库中查出
     */
    public void setMp3s(List<MP3> mp3s) {
        this.mp3s = mp3s;
        sureServiceAlive();
    }

    /**
     * 获取当前的播放模式
     *
     * @return 当前的播放模式
     */
    public int getPlayMode() {
        return playMode;
    }

    public MP3 getCurrentMP3() {
        return currentMP3;
    }

    public MP3 getCandidateNextMP3() {
        if (candidateNextMP3 == null) {
            candidateNextMP3 = pickCandidateNext(false);
        }
        return candidateNextMP3;
    }

    public MP3 getCandidatePreviousMP3() {
        if (candidatePreviousMP3 == null) {
            candidatePreviousMP3 = pickCandidatePrevious(false);
        }
        return candidatePreviousMP3;
    }

    public List<MP3> getMP3s() {
        return mp3s;
    }

    /**
     * 根据 mp3 获取封面
     *
     * @param mp3Bean 目标 mp3
     * @return 封面 bitmap
     */
    public Bitmap getAlbumart(MP3 mp3Bean) {
        Bitmap albumArtBitMap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {

            Uri uri = Uri
                    .parse("content://media/external/audio/albumart/" + mp3Bean.getAlbumId());

            ParcelFileDescriptor pfd = mContext.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                        options);
                pfd = null;
                fd = null;
            }
        } catch (Exception e) {
        }
        return albumArtBitMap;
    }


    /*
     * record/get independent volume settings
     */

    /**
     * 记录当前独立音量
     *
     * @param volume 独立音量，0 ～ 1
     */
    public void recordVolume(float volume) {
        mVolume = volume;
    }

    /**
     * 获取独立音量
     *
     * @return 独立音量，0 ～ 1
     */
    public float getVolume() {
        return mVolume;
    }

    /*
     * mp3s queue operations
     */

    /**
     * 缓存数据库中查询到的 queue 队列
     *
     * @return
     */
    public ArrayList<MP3> getQueueMP3s() {
        return mQueueMP3s;
    }

    /**
     * 重新设置播放队列
     *
     * @param queueMP3s 播放队列，一般从数据库中查出
     */
    public void setQueueMP3s(ArrayList<MP3> queueMP3s) {
        mQueueMP3s = queueMP3s;
    }

    /**
     * @param selectedMP3s 选中的 mp3 list_menu
     * @return 成功添加到播放队列的 mp3 条目数
     */
    public int addIntoQueue(ArrayList<MP3> selectedMP3s) {

        int success = 0;
        for (MP3 selectedMP3 : selectedMP3s) {
            if (!mQueueMP3s.contains(selectedMP3)) {
                mQueueMP3s.add(selectedMP3);
                success++;
            }
        }
        return success;
    }
}
