package yhb.chorus.alarm

import yhb.chorus.app.ChorusApplication
import yhb.chorus.common.utils.SharedPreferencesObject

private const val ALARM_SP_KEY = "alarm_sp_key"

object AlarmSpObject : SharedPreferencesObject(ChorusApplication.getsApplicationContext(), ALARM_SP_KEY) {
    var enable: Boolean by boolean(defaultValue = false)
    var song: String by string(defaultValue = "")
    var repeatDays: Set<String> by stringSet(defaultValue = HashSet())
    var alarmTime by long(defaultValue = System.currentTimeMillis())
}