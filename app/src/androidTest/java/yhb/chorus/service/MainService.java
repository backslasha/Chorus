package yhb.chorus.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import java.io.IOException;

import yhb.chorus.activity.MainActivity;
import yhb.chorus.utils.Utils;
import yhb.chorus.R;


public class MainService extends Service {
    public static final String ACTION_RENEW_PROGRESS = "ACTION_RENEW_PROGRESS";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_POINT = "ACTION_POINT";
    public static final String ACTION_EXIT = "ACTION_EXIT";
    public static final String ACTION_PROGRESS_CHANGE = "ACTION_PROGRESS_CHANGE";
    public static final String ACTION_CHANGE_FINISH = "ACTION_CHANGE_FINISH";
    private MediaPlayer mMediaPlayer;
    private Handler handler;
    private Runnable progressLaucher;
    private MainReceiver receiver;
    private Bitmap bitmap;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        mMediaPlayer = new MediaPlayer();
        receiver = new MainReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREVIOUS);
        intentFilter.addAction(ACTION_POINT);
        intentFilter.addAction(ACTION_EXIT);
        intentFilter.addAction(ACTION_PROGRESS_CHANGE);
        registerReceiver(receiver, intentFilter);

        foreSerLaunch();
        prepared();

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Utils.getInstance(MainService.this).next();
            }
        });

        //循环散播附带歌曲进度信息的广播
        progressLaucher = new Runnable() {
            @Override
            public void run() {
                try {
                    Intent msgIntent = new Intent(ACTION_RENEW_PROGRESS);
                    msgIntent.putExtra("curProgress", mMediaPlayer.getCurrentPosition());
                    msgIntent.putExtra("isPlaying", mMediaPlayer.isPlaying());
                    sendBroadcast(msgIntent);
                    handler.postDelayed(progressLaucher, 500);
                } catch (Exception e) {

                }
            }
        };
        handler.postDelayed(progressLaucher, 500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        unregisterReceiver(receiver);
        Intent msgIntent = new Intent(ACTION_RENEW_PROGRESS);
        msgIntent.putExtra("curProgress", 0);
        msgIntent.putExtra("isPlaying", false);
        sendBroadcast(msgIntent);
    }

    //mMediaPlayer控制接口
    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent serIntent = new Intent(context, MainService.class);
            startService(serIntent);
            switch (intent.getAction()) {
                case ACTION_PLAY_PAUSE:
                    if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    } else {
                        mMediaPlayer.pause();
                    }
                    foreSerLaunch();
                    break;
                case ACTION_POINT:
                    prepared();
                    mMediaPlayer.start();
                    foreSerLaunch();
                    sendBroadcast(new Intent("ACTION_CHANGE_FINISH"));
                    break;
                case ACTION_NEXT:
                    prepared();
                    mMediaPlayer.start();
                    foreSerLaunch();
                    sendBroadcast(new Intent("ACTION_CHANGE_FINISH"));
                    break;
                case ACTION_PREVIOUS:
                    prepared();
                    mMediaPlayer.start();
                    foreSerLaunch();
                    sendBroadcast(new Intent("ACTION_CHANGE_FINISH"));
                    break;
                case ACTION_EXIT:
                    stopSelf();
                    break;
                case ACTION_PROGRESS_CHANGE:
                    mMediaPlayer.seekTo(intent.getIntExtra("changeTo", 0));
                    break;
            }

        }
    }

    //准备好mMediaPlayer
    private void prepared() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(Utils.mp3Beans.get(Utils.currentPosition).getUri());
            mMediaPlayer.prepare();
            Utils.currentMP3 = Utils.mp3Beans.get(Utils.currentPosition);

        } catch (IndexOutOfBoundsException e) {
            Utils.mp3Beans = Utils.getInstance(this).getLocals();
            try {
                mMediaPlayer.setDataSource(Utils.mp3Beans.get(Utils.currentPosition).getUri());
                mMediaPlayer.prepare();
                Utils.currentMP3 = Utils.mp3Beans.get(Utils.currentPosition);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //启动前台服务的方法
    private void foreSerLaunch() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.content_notification);
        if (Utils.currentMP3 == null) {
            return;
        }
        remoteViews.setTextViewText(R.id.tv_not_title_id, Utils.currentMP3.getTitle());
        remoteViews.setTextViewText(R.id.tv_not_author_id, Utils.currentMP3.getArtist());
        if (mMediaPlayer.isPlaying()) {
            remoteViews.setImageViewResource(R.id.ibtn_play_or_pause_id, R.drawable.ic_pause_circle_small);
        } else {
            remoteViews.setImageViewResource(R.id.ibtn_play_or_pause_id, R.drawable.ic_play_circle_small);
        }
        if ((bitmap = Utils.getInstance(this).getAlbumart(Utils.currentMP3)) != null) {
            remoteViews.setImageViewBitmap(R.id.iv_artist_photo_id, bitmap);

        } else {
            remoteViews.setImageViewResource(R.id.iv_artist_photo_id, R.drawable.marry);
        }


        Intent broadIntent = new Intent(ACTION_EXIT);
        PendingIntent exitPi = PendingIntent.getBroadcast(this, 0, broadIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.ibtn_exit_id, exitPi);

        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent nextPi = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.ibtn_next_id, nextPi);

        Intent pSIntent = new Intent(ACTION_PLAY_PAUSE);
        PendingIntent pSPi = PendingIntent.getBroadcast(this, 0, pSIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.ibtn_play_or_pause_id, pSPi);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setTicker(Utils.currentMP3.getTitle()).setContent(remoteViews).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        Notification notification = builder.build();

        startForeground(1, notification);
    }


}
