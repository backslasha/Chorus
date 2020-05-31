package yhb.chorus.service

import yhb.chorus.app.ChorusApplication
import yhb.chorus.common.utils.SharedPreferencesObject
import yhb.chorus.main.MainPresenter

object PlayConfigSpObject : SharedPreferencesObject(ChorusApplication.getsApplicationContext(), "play_config_sp_key") {
    var independentVolume by int(defaultValue = MainPresenter.MAX_INDEPENDENT_VOLUME)
    var playMode by int(defaultValue = PlayCenter.MODE_LIST_LOOP)
    var mp3Json by string()
}