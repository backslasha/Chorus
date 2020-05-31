package yhb.chorus.alarm

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import yhb.chorus.alarm.AlarmTimeUtils.getNearestAlarmTimeFromRepeatDays
import yhb.chorus.common.utils.TimeDescHelper
import yhb.chorus.common.utils.toast
import yhb.chorus.databinding.ActivityAlarmBinding
import yhb.chorus.entity.MP3
import yhb.chorus.main.MainContract
import yhb.chorus.main.MainPresenter

class AlarmActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MP3 = "alarm_activity_extra_mp3"
        private const val TAG = "AlarmActivity"

        fun newIntent(context: Context, mp3: MP3): Intent {
            return Intent(context, AlarmActivity::class.java).apply {
                // fix class not found 问题
                // https://stackoverflow.com/questions/2307476/classnotfoundexception-when-using-custom-parcelable/21141830
                putExtra(EXTRA_MP3, Bundle().apply { putParcelable(EXTRA_MP3, mp3) })
            }
        }
    }

    private lateinit var mp3: MP3
    private lateinit var binding: ActivityAlarmBinding
    private lateinit var mainPresenter: MainPresenter
    private lateinit var viewModel: AlarmViewModel
    private val handler = Handler(Looper.getMainLooper())

    private val volumeIncreaseTask = object : Runnable {
        override fun run() {
            mainPresenter.maxVolumeSystem
            mainPresenter.currentVolumeSystem
            when {
                mainPresenter.currentVolumeSystem < mainPresenter.maxVolumeSystem -> {
                    mainPresenter.setVolumeSystem(mainPresenter.currentVolumeSystem + (mainPresenter.maxVolumeSystem * 0.1f).toInt())
                    handler.postDelayed(this, TimeDescHelper.SECONDS_OF_MINUTE * 1000L)
                    "系统音量增加10%.".toast(this@AlarmActivity)
                }
                mainPresenter.currentVolume < mainPresenter.maxVolume -> {
                    mainPresenter.setVolume(mainPresenter.currentVolume + (mainPresenter.maxVolume * 0.1f).toInt())
                    handler.postDelayed(this, TimeDescHelper.SECONDS_OF_MINUTE * 1000L)
                    "独立音量增加10%.".toast(this@AlarmActivity)
                }
                else -> {
                    Log.i(TAG, "volume has reached top.")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)
        viewModel.alarmConfigUpdated.observe(this, androidx.lifecycle.Observer {
            "下个闹钟将在${TimeDescHelper.desc(it)}响起.".toast(this)
        })
        mp3 = intent?.getBundleExtra(EXTRA_MP3)?.getParcelable(EXTRA_MP3) ?: return
        viewModel.updateAlarmConfig()
        mainPresenter = MainPresenter(this, object : MainContract.View {
            override fun getCoverSize(): Int {
                return 0
            }

            override fun invalidatePlayStatus(playing: Boolean, progress: Int) {

            }

            override fun invalidateWidgets(progress: Int, playMode: Int, songName: String?, artistName: String?) {

            }

            override fun setPresenter(presenter: MainContract.Presenter?) {

            }

            override fun invalidateCovers(bitmaps: Array<out Bitmap>?) {

            }

            override fun invalidateSeekBarVolumeSystem(currentVolume: Int, volumeSystemMax: Int) {

            }

        })
        mainPresenter.point(mp3)
        binding.tvMp3Name.text = mp3.title
        handler.postDelayed(volumeIncreaseTask, TimeDescHelper.SECONDS_OF_MINUTE * 1000L)
        "闹钟响起!".toast(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter.playOrPause()
        "闹钟取消.".toast(this)
    }
}

class AlarmViewModel : ViewModel() {
    val alarmConfigUpdated = MutableLiveData<Long>()
    fun updateAlarmConfig() {
        val repeatDays: Set<String> = AlarmSpObject.repeatDays
        val nextAlarmTime = getNearestAlarmTimeFromRepeatDays(repeatDays, AlarmSpObject.alarmTime)
        if (nextAlarmTime == 0L) { // 闹钟不重复
            AlarmSpObject.enable = false
            return
        }
        AlarmSpObject.alarmTime = nextAlarmTime
        alarmConfigUpdated.value = nextAlarmTime
    }
}