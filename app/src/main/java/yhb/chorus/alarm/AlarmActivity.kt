package yhb.chorus.alarm

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import yhb.chorus.common.utils.toast
import yhb.chorus.databinding.ActivityAlarmBinding
import yhb.chorus.entity.MP3
import yhb.chorus.main.MainContract
import yhb.chorus.main.MainPresenter

class AlarmActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MP3 = "alarm_activity_extra_mp3"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        mp3 = intent.getBundleExtra(EXTRA_MP3).getParcelable(EXTRA_MP3) ?: return
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
        "闹钟响起!".toast(this)
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}