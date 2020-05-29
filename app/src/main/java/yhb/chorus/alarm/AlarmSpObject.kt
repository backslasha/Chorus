package yhb.chorus.alarm

import yhb.chorus.app.ChorusApplication
import yhb.chorus.common.utils.SharedPreferencesObject

private const val ALARM_SP_KEY = "alarm_sp_key"

object AlarmSpObject : SharedPreferencesObject(ChorusApplication.getsApplicationContext(), ALARM_SP_KEY) {
    var enable by boolean()
    var song by string()
    var repeatDays by stringSet()
    var alarmTime by long(defaultValue = 0L)
}