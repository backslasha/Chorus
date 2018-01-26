package yhb.chorus.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import yhb.chorus.entity.MP3;


public class PlayCenter {

    @SuppressLint("StaticFieldLeak")
    private static PlayCenter sPlayCenter;
    private int currentPosition = 0;
    private int playMode = MODE_LIST_LOOP;
    private Context mContext;
    private float mVolume = 1f;

    public ArrayList<MP3> getQueueMP3s() {
        return mQueueMP3s;
    }

    private ArrayList<MP3> mQueueMP3s = new ArrayList<>();

    public List<MP3> getMp3s() {
        return mp3s;
    }

    private List<MP3> mp3s;
    private MP3 currentMP3;

    public static final int MODE_SINGLE_LOOP = 1;
    public static final int MODE_LIST_LOOP = 2;
    public static final int MODE_RANDOM = 3;

    private PlayCenter(Context context) {
        mContext = context;
    }

    public static PlayCenter getInstance(Context context) {
        if (sPlayCenter == null) {
            sPlayCenter = new PlayCenter(context);
        }
        return sPlayCenter;
    }

    public void next(boolean fromUser) {

        sureServiceAlive();

        currentMP3 = pickCandidateNext(fromUser);

        Intent intent = new Intent(MainService.ACTION_NEXT);

        mContext.sendBroadcast(intent);

    }

    public void previous(boolean fromUser) {

        sureServiceAlive();

        currentMP3 = pickCandidatePrevious(fromUser);

        Intent intent = new Intent(MainService.ACTION_PREVIOUS);
        mContext.sendBroadcast(intent);
    }

    public void next() {

        sureServiceAlive();

        currentMP3 = pickCandidateNext(false);

        Intent intent = new Intent(MainService.ACTION_NEXT);

        mContext.sendBroadcast(intent);

    }

    public void previous() {

        sureServiceAlive();

        currentMP3 = pickCandidatePrevious(false);

        Intent intent = new Intent(MainService.ACTION_PREVIOUS);
        mContext.sendBroadcast(intent);
    }

    public void playOrPause() {

        if (currentMP3 == null) {
            currentMP3 = pickCandidateNext(false);
        }

        sureServiceAlive();

        Intent intent = new Intent(MainService.ACTION_PLAY_PAUSE);
        mContext.sendBroadcast(intent);
    }

    public void point(MP3 mp3) {
        if (mp3 == null) {
            return;
        }

        int index = mp3s.indexOf(mp3);
        if (-1 == index) {
            mp3s.add(mp3);
            index = mp3s.size() - 1;
        }

        currentPosition = index;

        currentMP3 = mp3;

        Intent intent = new Intent(MainService.ACTION_POINT);
        mContext.sendBroadcast(intent);
    }

    private MP3 pickCandidatePrevious(boolean fromUser) {
        if (playMode == MODE_LIST_LOOP) {
            if (currentPosition > 0) {
                currentPosition -= 1;
            } else {
                currentPosition = mQueueMP3s.size() - 1;
            }
        } else if (playMode == MODE_RANDOM) {
            currentPosition = (int) (mQueueMP3s.size() * Math.random());
        } else if (playMode == MODE_SINGLE_LOOP) {
            if (fromUser) {
                if (currentPosition > 0) {
                    currentPosition -= 1;
                } else {
                    currentPosition = mQueueMP3s.size() - 1;
                }
            }
        }

        return mQueueMP3s.get(currentPosition);
    }

    private MP3 pickCandidateNext(boolean fromUser) {

        if (playMode == PlayCenter.MODE_LIST_LOOP) {
            if (currentPosition + 1 <= mQueueMP3s.size() - 1) {
                currentPosition += 1;
            } else {
                currentPosition = 0;
            }
        } else if (playMode == PlayCenter.MODE_RANDOM) {
            currentPosition = (int) (mQueueMP3s.size() * Math.random());
        } else if (playMode == PlayCenter.MODE_SINGLE_LOOP) {
            if (fromUser) {
                if (currentPosition > 0) {
                    currentPosition += 1;
                } else {
                    currentPosition = 0;
                }
            }
        }
        return mQueueMP3s.get(currentPosition);
    }

    /**
     * 循环选中下一个播放模式
     */
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

    /**
     * 根据 mp3 获取封面
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

    /**
     * 设置当前正在播放/暂停的 mp3
     * @param currentMP3 当前正在播放/暂停的 mp3
     */
    void setCurrentMP3(MP3 currentMP3) {
        this.currentMP3 = currentMP3;
    }

    /**
     * 获取当前正在播放/暂停的 mp3
     * @return 当前正在播放/暂停的 mp3
     */
    public MP3 getCurrentMP3() {
        return currentMP3;
    }

    /**
     * 设置当前的本地列表 mp3
     * @param mp3s 本地列表 mp3，一般从数据库中查出
     */
    public void setMp3s(List<MP3> mp3s) {
        this.mp3s = mp3s;
        sureServiceAlive();
    }

    /**
     * 确保服务存活
     */
    private void sureServiceAlive() {
        Intent serIntent = new Intent(mContext, MainService.class);
        mContext.startService(serIntent);
    }

    /**
     * 获取当前的播放模式
     * @return 当前的播放模式
     * */
    public int getPlayMode() {
        return playMode;
    }

    /**
     * 记录当前独立音量
     * @param volume 独立音量，0 ～ 1
     */
    public void recordVolume(float volume) {
        mVolume = volume;
    }

    /**
     * 获取独立音量
     * @return 独立音量，0 ～ 1
     */
    public float getVolume() {
        return mVolume;
    }

    /**
     * 重新设置播放队列
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
