package yhb.chorus.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import yhb.chorus.ICallback
import yhb.chorus.IPlayer
import yhb.chorus.R
import yhb.chorus.entity.MP3
import yhb.chorus.main.MainActivity.Companion.newIntent
import yhb.chorus.utils.BitmapUtils
import yhb.chorus.utils.DensityUtils
import java.io.IOException

class PlayerService : Service() {

    private val sdkPlayer = MediaPlayer()
    private var receiver: ForegroundServiceHandlerReceiver? = null
    private lateinit var playerImpl: Player

    override fun onBind(intent: Intent): IBinder? {
        playerImpl = Player()
        sdkPlayer.setOnCompletionListener {
            val iCallback = playerImpl.clientDelegate
            try {
                iCallback.onComplete()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            Log.d(TAG, "onCompletion: ")
        }
        return playerImpl
    }

    override fun onCreate() {
        super.onCreate()
        receiver = ForegroundServiceHandlerReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(REMOTE_INTENT_PLAY_PAUSE)
        intentFilter.addAction(REMOTE_INTENT_EXIT)
        intentFilter.addAction(REMOTE_INTENT_NEXT)
        intentFilter.addAction(REMOTE_INTENT_PREVIOUS)
        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        sdkPlayer.release()
        unregisterReceiver(receiver)
    }

    private fun launchForegroundService(currentMP3: MP3?) {
        val remoteViews = RemoteViews(packageName, R.layout.content_notification)
        val remoteViewsBig = RemoteViews(packageName, R.layout.content_notification_big)
        if (currentMP3 == null) {
            return
        }
        setupRemoteView(remoteViews, currentMP3)
        setupRemoteView(remoteViewsBig, currentMP3)
        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setTicker(currentMP3.title)
                .setContent(remoteViews)
                .setCustomBigContentView(remoteViewsBig)
                .setContentIntent(PendingIntent.getActivity(this, 0, newIntent(this), 0))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()
        startForeground(1, notification)
    }

    private fun setupRemoteView(remoteViews: RemoteViews, currentMP3: MP3) {
        remoteViews.setTextViewText(R.id.tv_not_title_id, currentMP3.title)
        remoteViews.setTextViewText(R.id.tv_not_author_id, currentMP3.artist)
        if (sdkPlayer.isPlaying) {
            remoteViews.setImageViewResource(R.id.image_button_play_or_pause, R.drawable.ic_pause_circle_outline)
        } else {
            remoteViews.setImageViewResource(R.id.image_button_play_or_pause, R.drawable.ic_play_circle_outline)
        }
        val size = DensityUtils.dp2px(this, 108f)
        remoteViews.setImageViewBitmap(R.id.iv_artist_photo_id,
                BitmapUtils.getAlbumart(currentMP3, size, size))
        val broadIntent = Intent(REMOTE_INTENT_EXIT)
        val exitPi = PendingIntent.getBroadcast(this, 0, broadIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.image_button_stop, exitPi)
        val nextIntent = Intent(REMOTE_INTENT_NEXT)
        val nextPi = PendingIntent.getBroadcast(this, 0, nextIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.image_button_next, nextPi)
        val previousIntent = Intent(REMOTE_INTENT_PREVIOUS)
        val previousPi = PendingIntent.getBroadcast(this, 0, previousIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.image_button_previous, previousPi)
        val pSIntent = Intent(REMOTE_INTENT_PLAY_PAUSE)
        val pSPi = PendingIntent.getBroadcast(this, 0, pSIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.image_button_play_or_pause, pSPi)
    }

    /**
     * receive remote intent of controlling the media player
     */
    internal inner class ForegroundServiceHandlerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (intent.action != null) {
                    playerImpl.clientDelegate.onNewRemoteIntent(intent.action)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    inner class Player : IPlayer.Stub() {

        var clientDelegate: ICallback = object : ICallback {
            override fun onComplete() {
                Log.i(TAG, "onComplete empty implement")
            }

            override fun onNewCurrent() {
                Log.i(TAG, "onNewCurrent empty implement")
            }

            override fun onNewRemoteIntent(aciton: String?) {
                Log.i(TAG, "onNewRemoteIntent empty implement")
            }

            override fun asBinder(): IBinder? {
                return null
            }

        }

        private var isPaused = false

        override fun playOrPause(mp3: MP3) {
            if (isPaused) {
                sdkPlayer.start()
                isPaused = false
            } else if (!sdkPlayer.isPlaying) {
                newCurrent(mp3)
            } else {
                sdkPlayer.pause()
                isPaused = true
            }
            launchForegroundService(mp3)
        }

        override fun setVolume(volume: Float) {
            sdkPlayer.setVolume(volume, volume)
        }

        override fun seekTo(progress: Int) {
            sdkPlayer.seekTo(progress)
        }

        @Throws(RemoteException::class)
        override fun isPlaying(): Boolean {
            return try {
                sdkPlayer.isPlaying
            } catch (e: IllegalStateException) {
                false
            }
        }

        @Throws(RemoteException::class)
        override fun getProgress(): Int {
            return if (isPlaying || isPaused) {
                sdkPlayer.currentPosition
            } else -1
        }

        @Throws(RemoteException::class)
        override fun registerCallback(callback: ICallback) {
            clientDelegate = callback
        }

        override fun newCurrent(mp3: MP3) {
            isPaused = false
            preparedAndStart(mp3)
        }

        private fun preparedAndStart(currentMP3: MP3) {
            try {
                sdkPlayer.reset()
                sdkPlayer.setDataSource(currentMP3.uri)
                sdkPlayer.prepareAsync()
                sdkPlayer.setOnPreparedListener {
                    sdkPlayer.start()
                    launchForegroundService(currentMP3)
                    try {
                        clientDelegate.onNewCurrent()
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val REMOTE_INTENT_PLAY_PAUSE = "REMOTE_INTENT_PLAY_PAUSE"
        const val REMOTE_INTENT_NEXT = "REMOTE_INTENT_NEXT"
        const val REMOTE_INTENT_PREVIOUS = "REMOTE_INTENT_PREVIOUS"
        const val REMOTE_INTENT_EXIT = "REMOTE_INTENT_EXIT"
        private const val TAG = "PlayerService"
    }
}