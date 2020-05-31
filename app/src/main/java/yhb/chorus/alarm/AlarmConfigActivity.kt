package yhb.chorus.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import yhb.chorus.R
import yhb.chorus.alarm.AlarmTimeUtils.getNearestAlarmTime
import yhb.chorus.common.utils.TimeDescHelper
import yhb.chorus.common.utils.TimeDescHelper.WeekDayNames
import yhb.chorus.common.utils.toast
import yhb.chorus.databinding.ActivityAlarmConfigBinding
import yhb.chorus.entity.MP3
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

    private fun bindViewModel(viewModel: AlarmConfigViewModel) {
        viewModel.alarmTime().observe(this, androidx.lifecycle.Observer {
            binding.tvAlarmTime.text = SimpleDateFormat("hh:mm", Locale.CHINA).format(calendar(it).time)
            viewModel.cancelCountdownTimer()
            viewModel.startCountdownTimer()
        })
        viewModel.alarmCountDownTime().observe(this, androidx.lifecycle.Observer {
            binding.tvAlarmTimeCountdown.text = DateUtils.formatElapsedTime(it / 1000)
        })
        viewModel.alarmCountDownFinish().observe(this, androidx.lifecycle.Observer {
            finish()
        })
        viewModel.song().observe(this, androidx.lifecycle.Observer {
            binding.tvAlarmSongContent.text = if (it == null) "请选择铃声" else "${it.title} - ${it.artist}"
        })
        viewModel.enable().observe(this, androidx.lifecycle.Observer {
            binding.switchAlarmStatus.isChecked = it
            viewModel.cancelCountdownTimer()
            if (!it) {
                binding.tvAlarmTimeCountdown.visibility = View.GONE
            } else {
                binding.tvAlarmTimeCountdown.visibility = View.VISIBLE
                viewModel.startCountdownTimer()
            }
        })
        viewModel.repeatDays().observe(this, androidx.lifecycle.Observer { repeatDays ->
            binding.tvAlarmRepeatModeContent.text = TimeDescHelper.weekDesc(repeatDays)
        })
    }

    private fun setupViews(binding: ActivityAlarmConfigBinding) {
        binding.flAlarmRepeatMode.setOnClickListener {
            val repeatDays = viewModel.repeatDays().value ?: HashSet()
            var resultSet: Set<String> = HashSet(repeatDays)
            MaterialAlertDialogBuilder(this)
                    .setTitle("规律作息，从我做起！")
                    .setMultiChoiceItems(WeekDayNames,
                            WeekDayNames.map { repeatDays.contains(it) }.toBooleanArray()) { _, which, isChecked ->
                        resultSet = if (isChecked) {
                            resultSet.plus(WeekDayNames[which])
                        } else {
                            resultSet.minus(WeekDayNames[which])
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
                        calendar.time = Date(System.currentTimeMillis())
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        viewModel.updateAlarmTime(getNearestAlarmTime(calendar.timeInMillis))
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
            val mp3 = viewModel.song().value
            if (mp3 == null) {
                binding.switchAlarmStatus.isChecked = false
                "请选择铃声!".toast(this)
                return@setOnClickListener
            }
            viewModel.updateAlarmEnable(enabled)
            val alarmTime = getNearestAlarmTime(viewModel.alarmTime().value!!).also {
                viewModel.updateAlarmTime(it)
            }
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (enabled) {
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(alarmTime, playMusicIntent(mp3)), playMusicIntent(mp3))
                "闹钟将在 ${TimeDescHelper.desc(alarmTime)} 响起.".toast(this@AlarmConfigActivity)
            } else {
                alarmManager.cancel(playMusicIntent(mp3))
                "闹钟已经取消.".toast(this@AlarmConfigActivity)
            }
        }
    }

    private fun playMusicIntent(mp3: MP3): PendingIntent {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val mp3 = viewModel.song().value
            if (mp3 == null) {
                binding.switchAlarmStatus.isChecked = false
                "请选择铃声!".toast(this)
                return true
            }
            val alarmTime = System.currentTimeMillis() + 4000
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, playMusicIntent(mp3))
            "闹钟将在 ${TimeDescHelper.desc(alarmTime)} 响起.".toast(this@AlarmConfigActivity)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun calendar(timeInMillis: Long = System.currentTimeMillis()) = Calendar.getInstance().apply {
        setTimeInMillis(timeInMillis)
    }
}

object AlarmConstants {
    const val REQUEST_CODE_AUTO_PLAY: Int = 1
    const val REQUEST_CODE_CHOOSE_SONG: Int = 0
    const val KEY_SONG_CHOSEN: String = "key_song_chosen"
}