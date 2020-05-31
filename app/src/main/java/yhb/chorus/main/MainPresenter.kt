package yhb.chorus.main

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.provider.Settings
import org.litepal.crud.DataSupport
import org.litepal.crud.callback.FindMultiCallback
import yhb.chorus.R
import yhb.chorus.entity.MP3
import yhb.chorus.entity.MP3InQueue
import yhb.chorus.service.PlayCenter
import yhb.chorus.service.PlayCenter.instance
import java.util.*

/**
 * Created by yhb on 18-1-17.
 */
class MainPresenter(private val mContext: Context, private val mView: MainContract.View) : MainContract.Presenter {

    private val settingsContentObserver: ContentObserver
    private val playCenter: PlayCenter
    private val audioManager: AudioManager

    override fun start() {
        if (playCenter.MP3s.size == 0) {
            loadMP3sFromDBAsync(playCenter)
            loadQueueMP3sFromDBAsync(playCenter)
        }
        mView.invalidateSeekBarVolumeSystem(currentVolumeSystem, maxVolumeSystem)
    }

    override fun release() {
        mContext.contentResolver.unregisterContentObserver(settingsContentObserver)
    }

    /*
     * load and save data from memory/db methods
     */
    override fun loadQueueMP3sFromDBAsync(playCenter: PlayCenter) {
        DataSupport.findAllAsync(MP3InQueue::class.java, true).listen(object : FindMultiCallback {
            override fun <T> onFinish(t: List<T>) {
                val mp3InQueue = t as List<MP3InQueue>
                val queueMP3s = ArrayList<MP3>()
                for (inQueue in mp3InQueue) {
                    queueMP3s.add(inQueue.mp3)
                }
                playCenter.queueMP3s = queueMP3s
            }
        })
    }

    override fun loadMP3sFromDBAsync(playCenter: PlayCenter) {
        DataSupport.findAllAsync(MP3::class.java).listen(object : FindMultiCallback {
            override fun <T> onFinish(t: List<T>) {
                playCenter.setMp3s((t as List<MP3>))
            }
        })
    }

    override fun loadQueueMP3sFromMemory(): List<MP3> {
        return playCenter.queueMP3s
    }

    override fun getCurrentMP3(): MP3? {
        return playCenter.currentMP3
    }

    /*
     * setting method
     */
    override fun loadSavedSetting() {
        // todo LoadSaveSetting
    }

    override fun setVolumeSystem(volume: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
    }

    override fun reloadCurrentWidgetsData() {
        val progress = playCenter.volumeIndependent as Int
        val playMode = playCenter.playMode
        val songName: String?
        val artistName: String?
        val currentMP3 = playCenter.currentMP3
        if (currentMP3 != null) {
            songName = currentMP3.title
            artistName = currentMP3.artist
        } else {
            songName = mContext.resources.getString(R.string.song_name)
            artistName = mContext.resources.getString(R.string.artist_name)
        }
        mView.invalidateWidgets(progress, playMode, songName, artistName)
    }

    override fun loadCoversAsync() {
        Thread(Runnable {
            val coverSize = mView.coverSize
            mView.invalidateCovers(playCenter.loadCovers(
                    coverSize, coverSize
            ))
        }).start()
    }

    override fun reloadConsoleData() {
        mView.invalidatePlayStatus(playCenter.isPlaying, playCenter.progress)
    }

    override fun setVolume(volume: Int) {
        playCenter.volumeIndependent = volume
    }

    override fun getMaxVolume(): Int {
        return MAX_INDEPENDENT_VOLUME
    }

    override fun getCurrentVolume(): Int {
        return playCenter.volumeIndependent
    }

    override fun getCurrentVolumeSystem(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    override fun getMaxVolumeSystem(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    /*
     * music control methods
     */
    override fun next() {
        playCenter.next(true)
    }

    override fun playOrPause() {
        playCenter.playOrPause()
    }

    override fun seekTo(progress: Int) {
        playCenter.seekTo(progress)
    }

    override fun previous() {
        playCenter.previous(true)
    }

    override fun point(mp3: MP3) {
        playCenter.point(mp3)
    }

    override fun nextPlayMode() {
        playCenter.nextPlayMode()
    }

    companion object {
        const val MAX_INDEPENDENT_VOLUME = 100
    }

    init {
        mView.setPresenter(this)
        playCenter = instance
        audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        settingsContentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                mView.invalidateSeekBarVolumeSystem(currentVolumeSystem, maxVolumeSystem)
                super.onChange(selfChange)
            }
        }
        mContext.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                settingsContentObserver
        )
    }
}