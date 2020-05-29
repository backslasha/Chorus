package yhb.chorus.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.json.JSONException
import yhb.chorus.R
import yhb.chorus.alarm.AlarmConstants.WEEK_DAYS
import yhb.chorus.common.utils.TimeDescHelper
import yhb.chorus.common.utils.toast
import yhb.chorus.databinding.ActivityAlarmConfigBinding
import yhb.chorus.entity.MP3
import yhb.chorus.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*


class AlarmConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmConfigBinding
    private lateinit var viewModel: AlarmConfigViewModel

    companion object {

        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, AlarmConfigActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmConfigBinding.inflate(layoutInflater)
        viewModel = ViewModelProviders.of(this).get(AlarmConfigViewModel::class.java)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setupViews(binding)
        bindViewModel(viewModel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("yaohaibiao","ondestory")
    }

    private fun bindViewModel(viewModel: AlarmConfigViewModel) {
        viewModel.alarmTime().observe(this, androidx.lifecycle.Observer {
            binding.tvAlarmTime.text = SimpleDateFormat("hh:mm", Locale.CHINA).format(calendar(it).time)
        })
        viewModel.song().observe(this, androidx.lifecycle.Observer {
            binding.tvAlarmSongContent.text = it?.title ?: "请选择铃声"
        })
        viewModel.enable().observe(this, androidx.lifecycle.Observer {
            binding.switchAlarmStatus.isChecked = it
        })
    }

    private fun setupViews(binding: ActivityAlarmConfigBinding) {
        binding.flAlarmRepeatMode.setOnClickListener {
            val repeatDays = viewModel.repeatDays().value ?: HashSet()
            var resultSet: Set<String> = HashSet(repeatDays)
            MaterialAlertDialogBuilder(this)
                    .setTitle("规律作息，从我做起！")
                    .setMultiChoiceItems(WEEK_DAYS,
                            WEEK_DAYS.map { repeatDays.contains(it) }.toBooleanArray()) { _, which, isChecked ->
                        resultSet = if (isChecked) {
                            resultSet.plus(WEEK_DAYS[which])
                        } else {
                            resultSet.minus(WEEK_DAYS[which])
                        }
                    }
                    .setPositiveButton(R.string.ok) { _, _ ->
                        viewModel.updateAlarmRepeatDays(resultSet)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .show()
        }
        binding.rlAlarmTime.setOnClickListener {
            val calendar = calendar(viewModel.alarmTime().value ?: System.currentTimeMillis())
            TimePickerDialog(this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        viewModel.updateAlarmTime(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            ).show()
        }
        binding.flAlarmSong.setOnClickListener {
            startActivityForResult(
                    Intent(this, ChooseSongActivity::class.java), AlarmConstants.REQUEST_CODE_CHOOSE_SONG)
        }
        binding.switchAlarmStatus.setOnClickListener {
            val enabled = binding.switchAlarmStatus.isChecked
            viewModel.updateAlarmEnable(enabled)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (enabled) {
                val alarmTime = System.currentTimeMillis() + 5000
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, playMusicIntent())
                "闹钟将在 ${TimeDescHelper.desc(alarmTime)} 响起.".toast(this@AlarmConfigActivity)
            } else {
                alarmManager.cancel(playMusicIntent())
                "闹钟已经取消.".toast(this@AlarmConfigActivity)
            }
        }
    }

    private fun playMusicIntent(): PendingIntent {
        val mp3 = viewModel.song().value!!
        return PendingIntent.getActivity(this,
                AlarmConstants.REQUEST_CODE_AUTO_PLAY,
                AlarmActivity.newIntent(this, mp3),
                FLAG_ONE_SHOT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AlarmConstants.REQUEST_CODE_CHOOSE_SONG && resultCode == RESULT_OK) {
            viewModel.updateAlarmSong(data?.getParcelableExtra(AlarmConstants.KEY_SONG_CHOSEN) as MP3?)
        }
    }

    private fun calendar(timeInMillis: Long = System.currentTimeMillis()) = Calendar.getInstance().apply {
        setTimeInMillis(timeInMillis)
    }
}

class AlarmConfigViewModel : ViewModel() {

    private val alarmTimeLiveData = MutableLiveData<Long>().apply {
        value = AlarmSpObject.alarmTime // init with sp value
    }
    private val repeatDaysLiveData = MutableLiveData<Set<String>>().apply {
        value = AlarmSpObject.repeatDays // init with sp value
    }
    private val alarmEnableLiveData = MutableLiveData<Boolean>().apply {
        value = AlarmSpObject.enable // init with sp value
    }
    private val songLiveData = MutableLiveData<MP3>().apply {
        value = try {
            Gson().fromJson(AlarmSpObject.song, MP3::class.java)
        } catch (e: JSONException) {
            null
        } // init with sp value
    }

    fun alarmTime(): LiveData<Long> = alarmTimeLiveData
    fun repeatDays(): LiveData<Set<String>> = repeatDaysLiveData
    fun song(): LiveData<MP3> = songLiveData
    fun enable(): LiveData<Boolean> = alarmEnableLiveData

    fun updateAlarmTime(timeInMillis: Long) {
        AlarmSpObject.alarmTime = timeInMillis
        alarmTimeLiveData.value = timeInMillis
    }

    fun updateAlarmEnable(enable: Boolean) {
        AlarmSpObject.enable = enable
        if (enable != alarmEnableLiveData.value) {
            alarmEnableLiveData.value = enable
        }
    }

    fun updateAlarmRepeatDays(repeatDays: Set<String>) {
        repeatDaysLiveData.value = repeatDays
        AlarmSpObject.repeatDays = repeatDays
    }

    fun updateAlarmSong(mp3: MP3?) {
        mp3 ?: return
        AlarmSpObject.song = Gson().toJson(mp3)
        songLiveData.value = mp3
    }

}

object AlarmConstants {
    const val EXTRA_AUTO_PLAY_CHOSEN: String = "extra_auto_play_chosen"
    const val REQUEST_CODE_AUTO_PLAY: Int = 1
    const val REQUEST_CODE_CHOOSE_SONG: Int = 0
    const val KEY_SONG_CHOSEN: String = "key_song_chosen"
    val WEEK_DAYS = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
}