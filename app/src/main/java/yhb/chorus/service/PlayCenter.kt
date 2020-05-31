package yhb.chorus.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import yhb.chorus.ICallback
import yhb.chorus.IPlayer
import yhb.chorus.app.ChorusApplication
import yhb.chorus.entity.MP3
import yhb.chorus.main.MainActivity
import yhb.chorus.main.MainPresenter.Companion.MAX_INDEPENDENT_VOLUME
import yhb.chorus.utils.BitmapUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object PlayCenter {
    /*
      * three kinds of play mode
      */
    const val MODE_SINGLE_LOOP = 1
    const val MODE_LIST_LOOP = 2
    const val MODE_RANDOM = 3

    @JvmStatic
    val instance: PlayCenter = this

    var queueMP3s = ArrayList<MP3>()

    val MP3s = ArrayList<MP3>()

    var currentMP3: MP3? = null
        private set

    private var player: IPlayer? = null

    private var candidateNextMP3: MP3? = null
    private var candidatePreviousMP3: MP3? = null
    private val mExecutorService: ExecutorService
    private var mNewCurrent = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            try {
                player = IPlayer.Stub.asInterface(service)
                player?.registerCallback(object : ICallback.Stub() {
                    @Throws(RemoteException::class)
                    override fun onComplete() {
                        next(false)
                    }

                    @Throws(RemoteException::class)
                    override fun onNewCurrent() {
                        mNewCurrent = true
                    }

                    override fun onNewRemoteIntent(action: String) {
                        handleRemoteIntent(action)
                    }
                })
                currentMP3?.let { point(it) }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            player = null
        }
    }

    private fun handleRemoteIntent(action: String) {
        when (action) {
            PlayerService.REMOTE_INTENT_NEXT -> next(true)
            PlayerService.REMOTE_INTENT_PREVIOUS -> previous(true)
            PlayerService.REMOTE_INTENT_PLAY_PAUSE -> playOrPause()
            PlayerService.REMOTE_INTENT_EXIT -> {
                appContext.unbindService(serviceConnection)
                player = null
            }
        }
    }

    init {
        Log.d(MainActivity.TAG, "PlayCenter: created!")
        mExecutorService = Executors.newCachedThreadPool()
    }

    var playMode = MODE_LIST_LOOP
        private set

    private var mVolumeIndependent = MAX_INDEPENDENT_VOLUME
    private val appContext: Context = ChorusApplication.getsApplicationContext()


    /*
     * operations of controlling music mPlayer
     */
    fun playOrPause() {
        if (currentMP3 == null) {
            next(true)
            return
        }
        if (!ensureServiceBound) {
            return
        }
        try {
            player?.playOrPause(currentMP3)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun next(fromUser: Boolean) {
        if (candidateNextMP3 == null) {
            candidateNextMP3 = pickCandidatePrevious(fromUser)
        }
        currentMP3 = candidateNextMP3
        if (!ensureServiceBound) {
            return
        }
        newCurrentAsync(currentMP3)
        candidateNextMP3 = pickCandidateNext(fromUser)
        candidatePreviousMP3 = pickCandidatePrevious(fromUser)
    }

    private fun newCurrentAsync(mp3: MP3?) {
        mExecutorService.execute {
            try {
                player?.newCurrent(mp3)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    fun previous(fromUser: Boolean) {
        if (candidatePreviousMP3 == null) {
            candidatePreviousMP3 = pickCandidatePrevious(fromUser)
        }
        currentMP3 = candidatePreviousMP3
        if (!ensureServiceBound) {
            return
        }
        newCurrentAsync(currentMP3)
        candidateNextMP3 = pickCandidateNext(fromUser)
        candidatePreviousMP3 = pickCandidatePrevious(fromUser)
    }

    fun point(mp3: MP3) {
        if (!queueMP3s.contains(mp3)) {
            queueMP3s.add(mp3)
        }
        currentMP3 = mp3
        if (!ensureServiceBound) {
            return
        }
        newCurrentAsync(currentMP3)
        candidateNextMP3 = pickCandidateNext(true)
        candidatePreviousMP3 = pickCandidatePrevious(true)
    }

    fun nextPlayMode() {
        playMode = when (playMode) {
            MODE_LIST_LOOP -> MODE_RANDOM
            MODE_RANDOM -> MODE_SINGLE_LOOP
            MODE_SINGLE_LOOP -> MODE_LIST_LOOP
            else -> MODE_LIST_LOOP
        }
    }

    fun seekTo(progress: Int) {
        try {
            player?.seekTo(progress)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun pickCandidatePrevious(fromUser: Boolean): MP3? {
        var currentIndex = queueMP3s.indexOf(currentMP3)
        if (playMode == MODE_LIST_LOOP) {
            if (currentIndex > 0) {
                currentIndex -= 1
            } else {
                currentIndex = queueMP3s.size - 1
            }
        } else if (playMode == MODE_RANDOM) {
            currentIndex = (queueMP3s.size * Math.random()).toInt()
        } else if (playMode == MODE_SINGLE_LOOP) {
            if (fromUser) {
                if (currentIndex > 0) {
                    currentIndex -= 1
                } else {
                    currentIndex = queueMP3s.size - 1
                }
            }
        }
        return if (currentIndex < 0 || currentIndex >= queueMP3s.size) {
            null
        } else queueMP3s[currentIndex]
    }

    private fun pickCandidateNext(fromUser: Boolean): MP3? {
        var currentIndex = queueMP3s.indexOf(currentMP3)
        if (playMode == MODE_LIST_LOOP) {
            if (currentIndex + 1 <= queueMP3s.size - 1) {
                currentIndex += 1
            } else {
                currentIndex = 0
            }
        } else if (playMode == MODE_RANDOM) {
            currentIndex = (queueMP3s.size * Math.random()).toInt()
        } else if (playMode == MODE_SINGLE_LOOP) {
            if (fromUser) {
                if (currentIndex > 0) {
                    currentIndex += 1
                } else {
                    currentIndex = 0
                }
            }
        }
        return if (currentIndex < 0 || currentIndex >= queueMP3s.size) {
            null
        } else queueMP3s[currentIndex]
    }

    val isPlaying: Boolean
        get() {
            try {
                return player?.isPlaying ?: false
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return false
        }

    val progress: Int
        get() {
            try {
                return player?.progress ?: 0
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return -1
        }

    private val ensureServiceBound: Boolean
        get() {
            if (player == null) {
                val intent = Intent(appContext, PlayerService::class.java)
                appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                return false
            }
            return true
        }
    /*
     * global public data createViewHolder/set methods
     */
    /**
     * 设置当前的本地列表 mp3
     *
     * @param mp3s 本地列表 mp3，一般从数据库中查出
     */
    fun setMp3s(mp3s: List<MP3>) {
        MP3s.clear()
        MP3s.addAll(mp3s)
    }

    private fun getCandidateNextMP3(): MP3? {
        if (candidateNextMP3 == null) {
            candidateNextMP3 = pickCandidateNext(false)
        }
        return candidateNextMP3
    }

    private fun getCandidatePreviousMP3(): MP3? {
        if (candidatePreviousMP3 == null) {
            candidatePreviousMP3 = pickCandidatePrevious(false)
        }
        return candidatePreviousMP3
    }

    fun loadCovers(reqWidth: Int, reqHeight: Int): Array<Bitmap?> {
        val currentMP3 = currentMP3
        val candidateNextMP3 = getCandidateNextMP3()
        val candidatePreviousMP3 = getCandidatePreviousMP3()
        val bitmaps: Array<Bitmap?> = arrayOfNulls(3)
        bitmaps[0] = BitmapUtils.getAlbumart(candidatePreviousMP3, reqWidth, reqHeight)
        bitmaps[1] = BitmapUtils.getAlbumart(currentMP3, reqWidth, reqHeight)
        bitmaps[2] = BitmapUtils.getAlbumart(candidateNextMP3, reqWidth, reqHeight)
        return bitmaps
    }

    var volumeIndependent
        get() = mVolumeIndependent
        set(volume) {
            try {
                mVolumeIndependent = volume
                player?.setVolume(mVolumeIndependent.toFloat() / MAX_INDEPENDENT_VOLUME.toFloat())
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

    /**
     * 添加 selectedMP3s 中的曲目到内存中的 queue 中（去掉重复的），
     * 重复元素同时从 selectedMP3s 中去掉
     *
     * @param selectedMP3s 选中的 mp3 list_menu
     * @return 成功添加到播放队列的 mp3 条目数
     */
    fun addIntoQueue(selectedMP3s: ArrayList<MP3>): Int {
        for (selectedMP3 in selectedMP3s) {
            if (queueMP3s.contains(selectedMP3)) {
                selectedMP3s.remove(selectedMP3)
            }
        }
        queueMP3s.addAll(selectedMP3s)
        return selectedMP3s.size
    }

}