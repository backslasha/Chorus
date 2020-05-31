package yhb.chorus.common.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeDescHelper {

    val WeekDayNames = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val WeekDaysRefs: Map<Int, String> = mapOf(
            Calendar.SUNDAY to "周日",
            Calendar.MONDAY to "周一",
            Calendar.TUESDAY to "周二",
            Calendar.WEDNESDAY to "周三",
            Calendar.THURSDAY to "周四",
            Calendar.FRIDAY to "周五",
            Calendar.SATURDAY to "周六"
    )

    private const val SECONDS_OF_MINUTE = 60
    private const val SECONDS_OF_HOUR = SECONDS_OF_MINUTE * 60
    const val SECONDS_OF_DAY = SECONDS_OF_HOUR * 24L
    private val simpleDateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)

    fun desc(timeInMill: Long): String {
        val now = System.currentTimeMillis()
        return if (timeInMill - now < SECONDS_OF_DAY) { // 小于1天
            hourDesc(timeInMill - now)
        } else {
            simpleDateFormat.format(Date(timeInMill))
        }
    }

    private fun hourDesc(timeInMill: Long): String {
        val seconds: Int = (timeInMill / 1000).toInt()
        val minutes: Int = seconds / SECONDS_OF_MINUTE
        val hours: Int = seconds / SECONDS_OF_HOUR
        if (hours == 0 && minutes == 0) {
            return "$seconds 秒后"
        }
        if (hours == 0) {
            return "$minutes 分钟后"
        }
        return "$hours 小时 $minutes 分钟后"
    }

    fun weekDesc(repeatDays: Set<String>): String {
        if (repeatDays.size == WeekDayNames.size) {
            return "每天"
        }
        val builder = StringBuilder()
        val serialArray = ArrayList<String>()
        for ((index, weekDay) in WeekDayNames.withIndex()) {
            if (repeatDays.contains(weekDay)) {
                serialArray.add(weekDay)
            }
            if (WeekDayNames.lastIndex == index
                    || !repeatDays.contains(WeekDayNames[index + 1])) {
                if (serialArray.size <= 2) serialArray.forEach {
                    builder.append(it).append(", ")
                } else {
                    builder.append("${serialArray.first()}到${serialArray.last()}").append(", ")
                }
                serialArray.clear()
            }
        }
        return builder.removeSuffix(", ").toString()
    }
}
