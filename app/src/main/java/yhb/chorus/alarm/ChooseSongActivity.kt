package yhb.chorus.alarm

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import yhb.chorus.R
import yhb.chorus.common.adapter.SimpleAdapter
import yhb.chorus.common.adapter.base.SimpleHolder
import yhb.chorus.databinding.ActivityChooseSongBinding
import yhb.chorus.entity.MP3

class ChooseSongActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseSongBinding
    private lateinit var viewModel: SongsViewModel
    private lateinit var adapter: SimpleAdapter<MP3>
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseSongBinding.inflate(layoutInflater)
        viewModel = ViewModelProviders.of(this).get(SongsViewModel::class.java)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setupViews(binding)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.loadMp3s().add()
    }

    private fun setupViews(binding: ActivityChooseSongBinding) {
        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        binding.rvSongs.adapter = object : SimpleAdapter<MP3>(this, R.layout.item_mp3_simple) {
            override fun convert(holder: SimpleHolder, entity: MP3?) {
                val textView = holder.itemView as TextView
                textView.text = entity?.title
                textView.setOnClickListener { showPreviewDialog(entity) }
            }
        }.also { adapter = it }

        viewModel.mp3sLiveData().observe(this, Observer {
            adapter.performDataSetChanged(it)
        })
    }

    private fun showPreviewDialog(entity: MP3?) {
        entity ?: return
        setResult(RESULT_OK, Intent().apply {
            this.putExtra(AlarmConstants.KEY_SONG_CHOSEN, entity)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun Disposable.add() = compositeDisposable.add(this)
}

