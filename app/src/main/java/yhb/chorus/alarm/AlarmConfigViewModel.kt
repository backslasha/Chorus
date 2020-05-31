package yhb.chorus.alarm

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import org.json.JSONException
import yhb.chorus.entity.MP3

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
    private val alarmCountDownTimeLiveData = MutableLiveData<Long>()
    private val alarmCountDownTimeFinishedLiveData = MutableLiveData<Unit>()

    private var countDownTimer: CountDownTimer? = null

    fun alarmTime(): LiveData<Long> = alarmTimeLiveData
    fun repeatDays(): LiveData<Set<String>> = repeatDaysLiveData
    fun song(): LiveData<MP3> = songLiveData
    fun enable(): LiveData<Boolean> = alarmEnableLiveData
    fun alarmCountDownTime(): LiveData<Long> = alarmCountDownTimeLiveData
    fun alarmCountDownFinish(): LiveData<Unit> = alarmCountDownTimeFinishedLiveData

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

    fun cancelCountdownTimer() {
        val timer = countDownTimer
        timer ?: return
        timer.cancel()
        countDownTimer = null
    }

    fun startCountdownTimer() {
        val triggerTime = alarmTime().value ?: return
        val millisInFuture = triggerTime - System.currentTimeMillis()
        if (millisInFuture < 0) {
            return
        }
        countDownTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onFinish() {
                alarmCountDownTimeFinishedLiveData.value = Unit
            }

            override fun onTick(millisUntilFinished: Long) {
                alarmCountDownTimeLiveData.value = millisUntilFinished
            }
        }.start()
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

    override fun onCleared() {
        super.onCleared()
        cancelCountdownTimer()
    }
}