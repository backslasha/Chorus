package yhb.chorus.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;

import yhb.chorus.R;
import yhb.chorus.entity.MP3;
import yhb.chorus.main.MainActivity;

import static yhb.chorus.main.MainActivity.TAG;


public class MainService extends Service {
    public static final String ACTION_RENEW_PROGRESS = "ACTION_RENEW_PROGRESS";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_NEXT_FROM_REMOTE_VIEW = "ACTION_NEXT_FROM_REMOTE_VIEW";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_POINT = "ACTION_POINT";
    public static final String ACTION_EXIT = "ACTION_EXIT";
    public static final String ACTION_PROGRESS_CHANGE = "ACTION_PROGRESS_CHANGE";
    public static final String ACTION_CHANGE_FINISH = "ACTION_CHANGE_FINISH";
    public static final String ACTION_SET_VOLUME = "ACTION_SET_VOLUME";
    private MediaPlayer mMediaPlayer;
    private Handler handler;
    private Runnable progressLauncher;
    private MainReceiver receiver;
    private PlayCenter mPlayCenter;
    private boolean isBroadcasting = false, isPaused = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        receiver = new MainReceiver();
        mPlayCenter = PlayCenter.getInstance();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREVIOUS);
        intentFilter.addAction(ACTION_POINT);
        intentFilter.addAction(ACTION_EXIT);
        intentFilter.addAction(ACTION_PROGRESS_CHANGE);
        intentFilter.addAction(ACTION_SET_VOLUME);
        intentFilter.addAction(ACTION_NEXT_FROM_REMOTE_VIEW);
        registerReceiver(receiver, intentFilter);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "onCompletion: ");
                mPlayCenter.next(false);
            }
        });

        //循环散播附带歌曲进度信息的广播
        progressLauncher = new Runnable() {
            @Override
            public void run() {
                try {
                    Intent msgIntent = new Intent(ACTION_RENEW_PROGRESS);
                    msgIntent.putExtra("currentProgress", mMediaPlayer.getCurrentPosition());
                    msgIntent.putExtra("isPlaying", mMediaPlayer.isPlaying());
                    sendBroadcast(msgIntent);
                    handler.postDelayed(progressLauncher, 500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        handler.removeCallbacks(progressLauncher);
        unregisterReceiver(receiver);
        Intent msgIntent = new Intent(ACTION_RENEW_PROGRESS);
        msgIntent.putExtra("currentProgress", 0);
        msgIntent.putExtra("isPlaying", false);
        sendBroadcast(msgIntent);
    }

    //mMediaPlayer控制接口
    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isBroadcasting) {
                handler.postDelayed(progressLauncher, 500);
                isBroadcasting = true;
            }

            if (mMediaPlayer.isPlaying()) {
                isPaused = false;
            }

            if (intent.getAction() == null) {
                return;
            }
            switch (intent.getAction()) {
                case ACTION_PLAY_PAUSE:
                    if (isPaused) {
                        mMediaPlayer.start();
                    } else if (!mMediaPlayer.isPlaying()) {
                        prepared();
                        mMediaPlayer.start();
                    } else {
                        mMediaPlayer.pause();
                        isPaused = true;
                    }
                    foreSerLaunch();
                    break;
                case ACTION_POINT:
                    prepared();
                    mMediaPlayer.start();
                    foreSerLaunch();
                    sendBroadcast(new Intent(ACTION_CHANGE_FINISH));
                    break;
                case ACTION_NEXT:
                case ACTION_PREVIOUS:
                    prepared();
                    mMediaPlayer.start();
                    foreSerLaunch();
                    sendBroadcast(new Intent(ACTION_CHANGE_FINISH));
                    break;
                case ACTION_NEXT_FROM_REMOTE_VIEW:
                    mPlayCenter.next(true);
                    break;
                case ACTION_PROGRESS_CHANGE:
                    mMediaPlayer.seekTo(intent.getIntExtra("changeTo", 0));
                    break;
                case ACTION_SET_VOLUME:
                    int intExtra = intent.getIntExtra("progress", 0);
                    float volume = ((float) intExtra) / 10f;
                    mMediaPlayer.setVolume(volume, volume);
                    break;
            }

        }
    }

    //准备好 MediaPlayer
    private void prepared() {

        MP3 candidate = mPlayCenter.getCurrentMP3();

        if (candidate == null) {
            return;
        }

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(candidate.getUri());
            mMediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //启动前台服务的方法
    private void foreSerLaunch() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.content_notification);
        RemoteViews remoteViewsBig = new RemoteViews(getPackageName(), R.layout.content_notification_big);
        MP3 currentMP3 = mPlayCenter.getCurrentMP3();
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

        remoteViews.setImageViewBitmap(R.id.iv_artist_photo_id, mPlayCenter.getAlbumart(currentMP3));

        Intent broadIntent = new Intent(ACTION_EXIT);
        PendingIntent exitPi = PendingIntent.getBroadcast(this, 0, broadIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.image_button_stop, exitPi);

        Intent nextIntent = new Intent(ACTION_NEXT_FROM_REMOTE_VIEW);
        PendingIntent nextPi = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.image_button_next, nextPi);

        Intent pSIntent = new Intent(ACTION_PLAY_PAUSE);
        PendingIntent pSPi = PendingIntent.getBroadcast(this, 0, pSIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.image_button_play_or_pause, pSPi);
    }


}
