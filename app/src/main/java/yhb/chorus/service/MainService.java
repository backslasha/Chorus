package yhb.chorus.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;

import yhb.chorus.ICallback;
import yhb.chorus.IPlayer;
import yhb.chorus.R;
import yhb.chorus.entity.MP3;
import yhb.chorus.main.MainActivity;

import static yhb.chorus.main.MainActivity.TAG;
import static yhb.chorus.utils.MP3Utils.getAlbumart;


public class MainService extends Service {
    public static final String ACTION_RENEW_PROGRESS = "ACTION_RENEW_PROGRESS";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_POINT = "ACTION_POINT";
    public static final String ACTION_PROGRESS_CHANGE = "ACTION_PROGRESS_CHANGE";
    public static final String ACTION_CHANGE_FINISH = "ACTION_CHANGE_FINISH";
    public static final String ACTION_SET_VOLUME = "ACTION_SET_VOLUME";

    public static final String REMOTE_INTENT_PLAY_PAUSE = "REMOTE_INTENT_PLAY_PAUSE";
    public static final String REMOTE_INTENT_NEXT = "REMOTE_INTENT_NEXT";
    public static final String REMOTE_INTENT_PREVIOUS = "REMOTE_INTENT_PREVIOUS";
    public static final String REMOTE_INTENT_EXIT = "REMOTE_INTENT_EXIT";

    private MediaPlayer mMediaPlayer;
    private ForegroundServiceHandlerReceiver receiver;
    private Player mPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mPlayer = new Player();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mPlayer.mICallback != null) {
                    try {
                        mPlayer.mICallback.onComplete();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "onCompletion: ");
            }
        });
        return mPlayer;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new ForegroundServiceHandlerReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REMOTE_INTENT_PLAY_PAUSE);
        intentFilter.addAction(REMOTE_INTENT_EXIT);
        intentFilter.addAction(REMOTE_INTENT_NEXT);
        intentFilter.addAction(REMOTE_INTENT_PREVIOUS);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREVIOUS);
        intentFilter.addAction(ACTION_POINT);
        intentFilter.addAction(ACTION_PROGRESS_CHANGE);
        intentFilter.addAction(ACTION_SET_VOLUME);
        registerReceiver(receiver, intentFilter);

        mMediaPlayer = new MediaPlayer();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        unregisterReceiver(receiver);
    }

    /**
     * receive remote intent of controlling the media player
     */
    public class ForegroundServiceHandlerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (intent.getAction() != null) {
                    mPlayer.mICallback.onNewRemoteIntent(intent.getAction());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    private void foreSerLaunch(MP3 currentMP3) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.content_notification);
        RemoteViews remoteViewsBig = new RemoteViews(getPackageName(), R.layout.content_notification_big);
        if (currentMP3 == null) {
            return;
        }

        setupRemoteView(remoteViews, currentMP3);
        setupRemoteView(remoteViewsBig, currentMP3);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setTicker(currentMP3.getTitle())
                .setContent(remoteViews)
                .setCustomBigContentView(remoteViewsBig)
                .setContentIntent(PendingIntent.getActivity(this, 0, MainActivity.newIntent(this), 0))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();


        startForeground(1, notification);
    }

    private void setupRemoteView(RemoteViews remoteViews, MP3 currentMP3) {
        remoteViews.setTextViewText(R.id.tv_not_title_id, currentMP3.getTitle());
        remoteViews.setTextViewText(R.id.tv_not_author_id, currentMP3.getArtist());
        if (mMediaPlayer.isPlaying()) {
            remoteViews.setImageViewResource(R.id.image_button_play_or_pause, R.drawable.ic_pause_circle_outline);
        } else {
            remoteViews.setImageViewResource(R.id.image_button_play_or_pause, R.drawable.ic_play_circle_outline);
        }

        remoteViews.setImageViewBitmap(R.id.iv_artist_photo_id, getAlbumart(currentMP3));

        Intent broadIntent = new Intent(REMOTE_INTENT_EXIT);
        PendingIntent exitPi = PendingIntent.getBroadcast(this, 0, broadIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.image_button_stop, exitPi);

        Intent nextIntent = new Intent(REMOTE_INTENT_NEXT);
        PendingIntent nextPi = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.image_button_next, nextPi);

        Intent previousIntent = new Intent(REMOTE_INTENT_PREVIOUS);
        PendingIntent previousPi = PendingIntent.getBroadcast(this, 0, previousIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.image_button_previous, previousPi);

        Intent pSIntent = new Intent(REMOTE_INTENT_PLAY_PAUSE);
        PendingIntent pSPi = PendingIntent.getBroadcast(this, 0, pSIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.image_button_play_or_pause, pSPi);
    }

    public class Player extends IPlayer.Stub {

        private boolean isPaused = false;

        private ICallback mICallback;

        @Override
        public void playOrPause(MP3 mp3) {
            if (isPaused) {
                mMediaPlayer.start();
            } else if (!mMediaPlayer.isPlaying()) {
                prepared(mp3);
                mMediaPlayer.start();
            } else {
                mMediaPlayer.pause();
                isPaused = true;
            }
            foreSerLaunch(mp3);
        }

        @Override
        public void next(MP3 mp3) {
            newCurrent(mp3);
        }

        @Override
        public void previous(MP3 mp3) {
            newCurrent(mp3);
        }

        @Override
        public void point(MP3 mp3) {
            newCurrent(mp3);
        }

        @Override
        public void setVolume(float volume) {
            mMediaPlayer.setVolume(volume, volume);
        }

        @Override
        public void seekTo(int progress) {
            mMediaPlayer.seekTo(progress);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mMediaPlayer.isPlaying();
        }

        @Override
        public int getProgress() throws RemoteException {
            return mMediaPlayer.getCurrentPosition();
        }

        @Override
        public void registerCallback(yhb.chorus.ICallback callback) throws RemoteException {
            mICallback = callback;
        }


        private void newCurrent(MP3 mp3) {
            isPaused = false;
            prepared(mp3);
            mMediaPlayer.start();
            foreSerLaunch(mp3);
            try {
                mICallback.onNewCurrent();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }


        private void prepared(MP3 currentMP3) {
//            if (!isBroadcasting) {
//                handler.postDelayed(progressLauncher, 500);
//                isBroadcasting = true;
//            }

            if (currentMP3 == null) {
                return;
            }

            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(currentMP3.getUri());
                mMediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
