package yhb.chorus.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.util.List;

import yhb.chorus.entity.MP3;


public class PlayCenter {

    @SuppressLint("StaticFieldLeak")
    private static PlayCenter sPlayCenter;
    private int currentPosition = 0;
    private int playMode = MODE_LIST_LOOP;
    private Context mContext;

    public List<MP3> getMp3s() {
        return mp3s;
    }

    private List<MP3> mp3s;
    private MP3 currentMP3, candidateMP3;

    public static final int MODE_SINGLE_LOOP = 1;
    public static final int MODE_LIST_LOOP = 2;
    public static final int MODE_RANDOM = 3;
    private int mVolume;

    private PlayCenter(Context context) {
        mContext = context;
    }

    public static PlayCenter getInstance(Context context) {
        if (sPlayCenter == null) {
            sPlayCenter = new PlayCenter(context);
        }
        return sPlayCenter;
    }

    public void next() {

        sureServiceAlive();

        candidateMP3 = pickCandidateNext();

        Intent intent = new Intent(MainService.ACTION_NEXT);

        mContext.sendBroadcast(intent);

    }

    public void previous() {

        sureServiceAlive();

        candidateMP3 = pickCandidatePrevious();

        Intent intent = new Intent(MainService.ACTION_PREVIOUS);
        mContext.sendBroadcast(intent);
    }

    public void playOrPause() {

        if (candidateMP3 == null) {
            candidateMP3 = pickCandidateNext();
        }

        sureServiceAlive();

        Intent intent = new Intent(MainService.ACTION_PLAY_PAUSE);
        mContext.sendBroadcast(intent);
    }

    public void point(MP3 mp3) {
        if (mp3 == null) {
            return;
        }
        if (-1 == mp3s.indexOf(mp3)) {
            mp3s.add(mp3);
        }

        candidateMP3 = mp3;

        Intent intent = new Intent(MainService.ACTION_POINT);
        mContext.sendBroadcast(intent);
    }

    private MP3 pickCandidatePrevious() {
        if (playMode == MODE_LIST_LOOP) {
            if (currentPosition > 0) {
                currentPosition -= 1;
            } else {
                currentPosition = mp3s.size() - 1;
            }
        } else if (playMode == MODE_RANDOM) {
            currentPosition = (int) (mp3s.size() * Math.random());
        } else if (playMode == MODE_SINGLE_LOOP) {

        }

        return mp3s.get(currentPosition);
    }

    private MP3 pickCandidateNext() {

        if (playMode == PlayCenter.MODE_LIST_LOOP) {
            if (currentPosition + 1 <= mp3s.size() - 1) {
                currentPosition += 1;
            } else {
                currentPosition = 0;
            }
        } else if (playMode == PlayCenter.MODE_RANDOM) {
            currentPosition = (int) (mp3s.size() * Math.random());
        } else if (playMode == PlayCenter.MODE_SINGLE_LOOP) {

        }
        return mp3s.get(currentPosition);
    }


    public void nextPlayMode() {
        switch (playMode) {
            case MODE_LIST_LOOP:
                playMode = MODE_RANDOM;
                break;
            case MODE_RANDOM:
                playMode = MODE_SINGLE_LOOP;
                break;
            case MODE_SINGLE_LOOP:
                playMode = MODE_LIST_LOOP;
                break;
            default:
                playMode = MODE_LIST_LOOP;
                break;
        }
    }

    Bitmap getAlbumart(MP3 mp3Bean) {
        Bitmap albumArtBitMap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {

            Uri uri = Uri
                    .parse("content://media/external/audio/albumart/" + mp3s.get(currentPosition).getAlbumId());

            ParcelFileDescriptor pfd = mContext.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                        options);
                pfd = null;
                fd = null;
            }
        } catch (Exception e) {
        }
        return albumArtBitMap;
    }

    MP3 getCandidateMP3() {
        return candidateMP3;
    }

    void setCurrentMP3(MP3 currentMP3) {
        this.currentMP3 = currentMP3;
    }

    public MP3 getCurrentMP3() {
        return currentMP3;
    }

    public void setMp3s(List<MP3> mp3s) {
        this.mp3s = mp3s;
        sureServiceAlive();
    }

    private void sureServiceAlive() {
        Intent serIntent = new Intent(mContext, MainService.class);
        mContext.startService(serIntent);
    }

    public void updateCover(ImageView imageViewCover) {
        Bitmap albumart = getAlbumart(currentMP3);
        imageViewCover.setImageBitmap(albumart);
    }

    public int getPlayMode() {
        return playMode;
    }

    public int getVolume() {
        return mVolume;
    }

    public void setVolume(int volume) {
        mVolume = volume;
    }
}
