package yhb.chorus.common.utils


import android.content.Context
import android.widget.Toast


fun String.toast(context: Context) = Toast.makeText(context, this, Toast.LENGTH_SHORT).show()

object CommonUtils {

}