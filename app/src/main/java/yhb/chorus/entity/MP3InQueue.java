package yhb.chorus.entity;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by yhb on 18-2-1.
 */

public class MP3InQueue extends DataSupport {

    public MP3InQueue(MP3 mp3) {
        this.mp3 = mp3;
    }

    @Column(unique = true)
    private MP3 mp3;

    public MP3InQueue() {
    }

    public MP3 getMp3() {
        return mp3;
    }

    public void setMp3(MP3 mp3) {
        this.mp3 = mp3;
    }
}
