package yhb.chorus.alarm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.litepal.crud.DataSupport
import yhb.chorus.entity.MP3

class SongsViewModel : ViewModel() {

    private val tag = "SongsViewModel"

    private var mp3sLiveData = MutableLiveData<List<MP3>>()

    fun mp3sLiveData(): LiveData<List<MP3>> {
        return mp3sLiveData
    }

    fun loadMp3s(): Disposable {
        return Single.just(DataSupport.findAll(MP3::class.java))
                .subscribeOn(Schedulers.io())
                .subscribe { mp3s, throwable ->
                    if (throwable != null) {
                        Log.i(tag, "loadMp3s failed.", throwable)
                        return@subscribe
                    }
                    mp3sLiveData.postValue(mp3s)
                }
    }


}