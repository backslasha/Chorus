package yhb.chorus.main;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import static yhb.chorus.main.MainActivity.TAG;

/**
 * Created by yhb on 18-1-18.
 */
public class SettingsContentObserver extends ContentObserver {
    private int previousVolume;
    private Context context;
    private AudioManager audio;

    public SettingsContentObserver(Context c, Handler handler) {
        super(handler);
        context = c;
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int delta = previousVolume - currentVolume;
        if (delta > 0) {
            Log.d(TAG, "Decreased");
            previousVolume = currentVolume;
        } else if (delta < 0) {
            Log.d(TAG, "Increased");
            previousVolume = currentVolume;
        }
    }
}