package yhb.chorus.common.utils

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log

class ExitClearTaskActivity : Activity() {
    companion object {
        fun finishAndRemoveTask(activity: Activity) {
            val intent = Intent(activity, ExitClearTaskActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("yaohaibiao", "onCreate")
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("yaohaibiao", "onDestory")
    }
}