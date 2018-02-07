package yhb.chorus.service;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import yhb.chorus.ICallback;
import yhb.chorus.IPlayer;
import yhb.chorus.app.ChorusApplication;
import yhb.chorus.entity.MP3;

import static yhb.chorus.main.MainActivity.TAG;
import static yhb.chorus.service.MainService.REMOTE_INTENT_EXIT;
import static yhb.chorus.service.MainService.REMOTE_INTENT_NEXT;
import static yhb.chorus.service.MainService.REMOTE_INTENT_PLAY_PAUSE;
import static yhb.chorus.service.MainService.REMOTE_INTENT_PREVIOUS;
import static yhb.chorus.utils.BitmapUtils.getAlbumart;


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
    private int playMode = MODE_LIST_LOOP;
    private float mVolumeIndependent = 1f;

    private ArrayList<MP3> mQueueMP3s = new ArrayList<>();
    private List<MP3> mp3s;
    private MP3 currentMP3;
    private MP3 candidateNextMP3;
    private MP3 candidatePreviousMP3;
    private IPlayer mPlayer;

    private ExecutorService mExecutorService;

    private boolean mNewCurrent = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                mPlayer = MainService.Player.asInterface(service);
                mPlayer.registerCallback(new ICallback.Stub() {
                    @Override
                    public void onComplete() throws RemoteException {
                        next(false);
                    }

                    @Override
                    public void onNewCurrent() throws RemoteException {
                        mNewCurrent = true;
                    }

                    @Override
                    public void onNewRemoteIntent(String action) {
                        switch (action) {
                            case REMOTE_INTENT_NEXT:
                                next(true);
                                break;
                            case REMOTE_INTENT_PREVIOUS:
                                previous(true);
                                break;
                            case REMOTE_INTENT_PLAY_PAUSE:
                                playOrPause();
                                break;
                            case REMOTE_INTENT_EXIT:
                                mContext.unbindService(mConnection);
                                mPlayer = null;
                                break;
                        }
                    }

                });
                point(currentMP3);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayer = null;
        }
    };

    public static PlayCenter getInstance() {
        if (sPlayCenter == null) {
            sPlayCenter = new PlayCenter();
        }
        return sPlayCenter;
    }

    private PlayCenter() {
        Log.d(TAG, "PlayCenter: created!");
        mContext = ChorusApplication.getsApplicationContext();
        mExecutorService = Executors.newCachedThreadPool();
    }

    /*
     * operations of controlling music mPlayer
     */

    public void playOrPause() {

        if (currentMP3 == null) {
            next(true);
            return;
        }

        if (!isBinderHere()) {
            return;
        }

        try {
            mPlayer.playOrPause(currentMP3);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void next(boolean fromUser) {

        if (candidateNextMP3 == null) {
            candidateNextMP3 = pickCandidatePrevious(fromUser);
        }

        currentMP3 = candidateNextMP3;

        if (!isBinderHere()) {
            return;
        }

        newCurrentAsync(currentMP3);

        candidateNextMP3 = pickCandidateNext(fromUser);
        candidatePreviousMP3 = pickCandidatePrevious(fromUser);

    }

    private void newCurrentAsync(final MP3 mp3) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mPlayer.newCurrent(mp3);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void previous(boolean fromUser) {

        if (candidatePreviousMP3 == null) {
            candidatePreviousMP3 = pickCandidatePrevious(fromUser);
        }

        currentMP3 = candidatePreviousMP3;

        if (!isBinderHere()) {
            return;
        }

        newCurrentAsync(currentMP3);

        candidateNextMP3 = pickCandidateNext(fromUser);
        candidatePreviousMP3 = pickCandidatePrevious(fromUser);
    }

    public void point(MP3 mp3) {

        if (mp3 == null) {
            return;
        }

        if (!mQueueMP3s.contains(mp3)) {
            mQueueMP3s.add(mp3);
        }

        currentMP3 = mp3;

        if (!isBinderHere()) {
            return;
        }

        newCurrentAsync(currentMP3);

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

    public void seekTo(int progress) {
        if (mPlayer == null) {
            return;
        }
        try {
            mPlayer.seekTo(progress);
        } catch (RemoteException e) {
            e.printStackTrace();
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
//            Toast.makeText(mContext, "Queue is empty!", Toast.LENGTH_SHORT).show();
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
//            Toast.makeText(mContext, "Queue is empty!", Toast.LENGTH_SHORT).show();
            return null;
        }

        return mQueueMP3s.get(currentIndex);
    }

    public boolean isPlaying() {
        if (mPlayer == null) {
            return false;
        }
        try {
            return mPlayer.isPlaying();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getProgress() {
        if (mPlayer == null) {
            return -1;
        }
        try {
            return mPlayer.getProgress();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean isBinderHere() {
        if (mPlayer == null) {
            Intent intent = new Intent(mContext, MainService.class);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            return false;
        }
        return true;
    }

    /*
     * global public data createViewHolder/set methods
     */

    /**
     * 设置当前的本地列表 mp3
     *
     * @param mp3s 本地列表 mp3，一般从数据库中查出
     */
    public void setMp3s(List<MP3> mp3s) {
        this.mp3s = mp3s;
    }

    public List<MP3> getMP3s() {
        return mp3s;
    }

    public int getPlayMode() {
        return playMode;
    }

    public MP3 getCurrentMP3() {
        return currentMP3;
    }

    private MP3 getCandidateNextMP3() {
        if (candidateNextMP3 == null) {
            candidateNextMP3 = pickCandidateNext(false);
        }
        return candidateNextMP3;
    }

    private MP3 getCandidatePreviousMP3() {
        if (candidatePreviousMP3 == null) {
            candidatePreviousMP3 = pickCandidatePrevious(false);
        }
        return candidatePreviousMP3;
    }

    public Bitmap[] loadCovers(int reqWidth, int reqHeight) {
        Bitmap[] bitmaps;
        MP3 currentMP3 = getCurrentMP3();
        MP3 candidateNextMP3 = getCandidateNextMP3();
        MP3 candidatePreviousMP3 = getCandidatePreviousMP3();

        bitmaps = new Bitmap[3];

        bitmaps[0] = getAlbumart(candidatePreviousMP3, reqWidth, reqHeight);
        bitmaps[1] = getAlbumart(currentMP3, reqWidth, reqHeight);
        bitmaps[2] = getAlbumart(candidateNextMP3, reqWidth, reqHeight);

        return bitmaps;
    }

    /*
     * record/createViewHolder independent volume settings
     */

    /**
     * 记录当前独立音量
     *
     * @param volume 独立音量，0 ～ 1
     */
    public void setVolumeIndependent(float volume) {
        try {
            mPlayer.setVolume(volume);
            mVolumeIndependent = volume;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取独立音量
     *
     * @return 独立音量，0 ～ 1
     */
    public float getVolumeIndependent() {
        return mVolumeIndependent;
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
     * 添加 selectedMP3s 中的曲目到内存中的 queue 中（去掉重复的），
     * 重复元素同时从 selectedMP3s 中去掉
     *
     * @param selectedMP3s 选中的 mp3 list_menu
     * @return 成功添加到播放队列的 mp3 条目数
     */
    public int addIntoQueue(ArrayList<MP3> selectedMP3s) {

        for (MP3 selectedMP3 : selectedMP3s) {
            if (mQueueMP3s.contains(selectedMP3)) {
                selectedMP3s.remove(selectedMP3);
            }
        }

        mQueueMP3s.addAll(selectedMP3s);

        return selectedMP3s.size();
    }

}
