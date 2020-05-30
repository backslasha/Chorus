package yhb.chorus.main

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import yhb.chorus.R
import yhb.chorus.common.adapter.SimpleAdapter
import yhb.chorus.common.adapter.base.SimpleHolder
import yhb.chorus.databinding.FragmentMainBinding
import yhb.chorus.entity.MP3
import yhb.chorus.list.ListActivity
import yhb.chorus.service.PlayCenter.MODE_LIST_LOOP
import yhb.chorus.service.PlayCenter.MODE_RANDOM
import yhb.chorus.service.PlayCenter.MODE_SINGLE_LOOP
import yhb.chorus.utils.ActivityUtils

/**
 * Created by yhb on 18-1-17.
 */
class MainFragment : Fragment(), MainContract.View, View.OnClickListener {

    private lateinit var mainPresenter: MainContract.Presenter
    private lateinit var invalidateConsole: Runnable
    private lateinit var pageSelectedListener: PageSelectedListener

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding get() = _binding!!

    private val handler = Handler()
    private var coverSize = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        invalidateConsole = Runnable {
            try {
                mainPresenter.reloadConsoleData()
                handler.postDelayed(invalidateConsole, 500)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        coverSize = ActivityUtils.getScreenWidth(activity) * 4 / 5
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        bindViews(binding)
        mainPresenter.start()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mainPresenter.reloadCurrentWidgetsData()
        mainPresenter.loadSavedSetting()
        mainPresenter.loadCoversAsync()
        invalidateSeekBarVolumeSystem(
                mainPresenter.currentVolumeSystem,
                mainPresenter.maxVolumeSystem
        )
        handler.postDelayed(invalidateConsole, 500)
    }

    private fun bindViews(binding: FragmentMainBinding) {
        binding.slimSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            private var progress = -1
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    this.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (progress != -1) {
                    mainPresenter.seekTo(progress)
                    progress = -1
                }
            }
        })
        binding.slimSeekBarVolume.max = 10
        binding.slimSeekBarVolume.progress = 0
        binding.slimSeekBarVolume.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress.toFloat() / seekBar.max.toFloat()
                    mainPresenter.setVolume(volume)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.slimSeekBarVolumeSystem.max = mainPresenter.maxVolumeSystem
        binding.slimSeekBarVolumeSystem.progress = mainPresenter.currentVolumeSystem
        binding.slimSeekBarVolumeSystem.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mainPresenter.setVolumeSystem(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        val layoutParams = binding.viewPagerCover.layoutParams
        val coverSize = coverSize
        layoutParams.width = coverSize
        layoutParams.height = coverSize
        val coverPagerAdapter = CoverPagerAdapter()
        val initialIndex = coverPagerAdapter.count / 4 * 3 + 1
        pageSelectedListener = PageSelectedListener(
                initialIndex, object : OnSelectedListener {
            override fun onNext() {
                mainPresenter.next()
                mainPresenter.reloadCurrentWidgetsData()
                mainPresenter.loadCoversAsync()
                Log.d(MainActivity.TAG, "onNext: ")
            }

            override fun onPrevious() {
                Log.d(MainActivity.TAG, "onPrevious: ")
                mainPresenter.previous()
                mainPresenter.reloadCurrentWidgetsData()
                mainPresenter.loadCoversAsync()
            }
        }
        )
        binding.viewPagerCover.layoutParams = layoutParams
        binding.viewPagerCover.adapter = coverPagerAdapter
        binding.viewPagerCover.currentItem = initialIndex
        binding.viewPagerCover.addOnPageChangeListener(pageSelectedListener)
        binding.imageButtonNext.setOnClickListener(this)
        binding.imageButtonPrevious.setOnClickListener(this)
        binding.imageButtonPlayOrPause.setOnClickListener(this)
        binding.imageButtonPlayMode.setOnClickListener(this)
        binding.imageButtonQueueMusic.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter.release()
        handler.removeCallbacks(invalidateConsole)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_out -> {
                val intent = ListActivity.newIntent(activity)
                startActivity(intent)
            }
            else -> {
            }
        }
        return true
    }

    override fun setPresenter(presenter: MainContract.Presenter) {
        mainPresenter = presenter
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.image_button_next -> {
                mainPresenter.next()
                mainPresenter.reloadCurrentWidgetsData()
                mainPresenter.loadCoversAsync()
            }
            R.id.image_button_play_or_pause -> {
                mainPresenter.playOrPause()
                mainPresenter.reloadCurrentWidgetsData()
                mainPresenter.loadCoversAsync()
            }
            R.id.image_button_previous -> {
                mainPresenter.previous()
                mainPresenter.reloadCurrentWidgetsData()
                mainPresenter.loadCoversAsync()
            }
            R.id.image_button_play_mode -> {
                mainPresenter.nextPlayMode()
                mainPresenter.reloadCurrentWidgetsData()
            }
            R.id.image_button_queue_music -> showBottomSheet()
        }
    }

    override fun invalidateSeekBarVolumeSystem(currentVolume: Int, volumeSystemMax: Int) {
        binding.slimSeekBarVolumeSystem.max = volumeSystemMax
        binding.slimSeekBarVolumeSystem.progress = currentVolume
    }

    override fun invalidateCovers(bitmaps: Array<Bitmap>) {
        activity?.runOnUiThread {
            val covers = (binding.viewPagerCover.adapter as CoverPagerAdapter).covers
            val index = pageSelectedListener.currentIndex()
            covers[index].setImageBitmap(bitmaps[1])
            when (index) {
                0 -> {
                    covers[2].setImageBitmap(bitmaps[0])
                    covers[1].setImageBitmap(bitmaps[2])
                }
                1 -> {
                    covers[0].setImageBitmap(bitmaps[0])
                    covers[2].setImageBitmap(bitmaps[2])
                }
                2 -> {
                    covers[1].setImageBitmap(bitmaps[0])
                    covers[0].setImageBitmap(bitmaps[2])
                }
            }
        }
    }

    override fun invalidateWidgets(progress: Int, playMode: Int, songName: String, artistName: String) {
        binding.slimSeekBarVolume.progress = progress
        when (playMode) {
            MODE_LIST_LOOP -> binding.imageButtonPlayMode.setImageResource(R.drawable.ic_repeat_list)
            MODE_RANDOM -> binding.imageButtonPlayMode.setImageResource(R.drawable.ic_shuffle)
            MODE_SINGLE_LOOP -> binding.imageButtonPlayMode.setImageResource(R.drawable.ic_repeat_one)
            else -> binding.imageButtonPlayMode.setImageResource(R.drawable.ic_repeat_list)
        }
        binding.textViewSongName.text = songName
        binding.textViewArtistName.text = artistName
    }

    override fun invalidatePlayStatus(playing: Boolean, progress: Int) {
        val currentMP3 = mainPresenter.currentMP3 ?: return
        val maxProgress = currentMP3.duration
        binding.slimSeekBar.max = maxProgress
        binding.slimSeekBar.progress = progress
        binding.textViewMaxProgress.text = mm2min(maxProgress)
        binding.textViewCurrentProgress.text = mm2min(progress)
        if (playing) {
            binding.imageButtonPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline)
        } else {
            binding.imageButtonPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline)
        }
    }

    override fun getCoverSize(): Int {
        return coverSize
    }

    private var bottomSheetDialog: BottomSheetDialog? = null
    private val queueMP3SimpleAdapter: SimpleAdapter<MP3> by lazy {
        object : SimpleAdapter<MP3>(requireNotNull(activity), R.layout.item_mp3_simple) {
            override fun convert(holder: SimpleHolder, mp3: MP3) {
                val textView = holder.getView<TextView>(R.id.text_view_song_name)
                textView.text = String.format("%s / %s", mp3.title, mp3.artist)
                textView.setOnClickListener {
                    mainPresenter.point(mp3)
                    mainPresenter.reloadCurrentWidgetsData()
                    mainPresenter.loadCoversAsync()
                }
            }
        }
    }

    private fun showBottomSheet() {
        var sheetDialog = bottomSheetDialog
        if (sheetDialog != null) {
            queueMP3SimpleAdapter.performDataSetChanged(mainPresenter.loadQueueMP3sFromMemory())
            sheetDialog.show()
            return
        }
        val activity = activity ?: return
        val recyclerView = LayoutInflater.from(activity)
                .inflate(R.layout.content_queue_song, binding.root, false) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = queueMP3SimpleAdapter

        queueMP3SimpleAdapter.performDataSetChanged(mainPresenter.loadQueueMP3sFromMemory())

        sheetDialog = BottomSheetDialog(activity).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setContentView(recyclerView)
            bottomSheetDialog = sheetDialog
        }

        val window = sheetDialog.window ?: return
        // 曲线救国设置 peekHeight（出现时的高度）
        val maxHeight = ActivityUtils.getScreenHeight(activity) * 3 / 5
        val view = window.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        BottomSheetBehavior.from(view).peekHeight = maxHeight
        sheetDialog.show()

        // 设置最大高度
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, maxHeight)
        // 解决设置最大高度后的悬空问题
        window.setGravity(Gravity.BOTTOM)

    }

    private fun mm2min(mm: Int): String {
        val min: String = if (mm / 1000 / 60 > 9) {
            (mm / 1000 / 60).toString()
        } else {
            "0" + mm / 1000 / 60
        }
        val sec: String = if (mm / 1000 % 60 <= 9) {
            "0" + mm / 1000 % 60
        } else {
            ((mm / 1000) % 60).toString()
        }
        return "$min:$sec"
    }

    internal inner class CoverPagerAdapter : PagerAdapter() {

        val covers: Array<ImageView> = Array(3) { ImageView(this@MainFragment.activity) }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View = covers[position % 3]
            if (container == view.parent) {
                container.removeView(view)
            }
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
        override fun getCount(): Int {
            return Int.MAX_VALUE / 2
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        init {
            val screenWidth = ActivityUtils.getScreenWidth(activity)
            for (cover in covers) {
                cover.setBackgroundColor(Color.parseColor("#44888888"))
                cover.setPadding(screenWidth / 50, screenWidth / 50, screenWidth / 50, screenWidth / 50)
                cover.scaleType = ImageView.ScaleType.FIT_XY
            }
        }
    }

    internal interface OnSelectedListener {
        fun onNext()
        fun onPrevious()
    }

    internal inner class PageSelectedListener(private var oldIndex: Int, private val mOnSelectedListener: OnSelectedListener) : OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            if (position == oldIndex + 1) {
                mOnSelectedListener.onNext()
            } else if (position == oldIndex - 1) {
                mOnSelectedListener.onPrevious()
            }
            oldIndex = position
        }

        override fun onPageScrollStateChanged(state: Int) {}
        fun currentIndex(): Int {
            return oldIndex % 3
        }

    }

    companion object {

        @JvmStatic
        fun newInstance(): MainFragment {
            val args = Bundle()
            val fragment = MainFragment()
            fragment.arguments = args
            return fragment
        }
    }
}