package yhb.chorus.alarm

import yhb.chorus.common.utils.TimeDescHelper
import java.util.*

object AlarmTimeUtils {

    fun getNearestAlarmTime(fromWhen: Long): Long = if (fromWhen > System.currentTimeMillis()) {
        fromWhen // 今天的闹钟
    } else {
        fromWhen + TimeDescHelper.SECONDS_OF_DAY * 1000L
    }

    fun getNearestAlarmTimeFromRepeatDays(repeatDays: Set<String>, fromWhen: Long): Long {
        if (repeatDays.isEmpty()) { // 闹钟不重复
            return 0L
        }
        val calendar = Calendar.getInstance().apply {
            timeInMillis = fromWhen
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        if (repeatDays.contains(TimeDescHelper.WeekDaysRefs[dayOfWeek])
                && fromWhen > System.currentTimeMillis()) { // 就是今天了
            return fromWhen
        }
        val nextDayOfWeek = fun(): Int {
            var nextDay: Int
            do {
                nextDay = (dayOfWeek + 1) % TimeDescHelper.WeekDaysRefs.size
                if (repeatDays.contains(TimeDescHelper.WeekDaysRefs[nextDay])) {
                    return nextDay
                }
            } while (true)
        }()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.DAY_OF_WEEK, nextDayOfWeek)
        return calendar.timeInMillis
    }
}