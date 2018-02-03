package yhb.chorus.utils;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;

import yhb.chorus.R;
import yhb.chorus.app.ChorusApplication;
import yhb.chorus.entity.MP3;

/**
 * Created by yhb on 18-2-3.
 */

public class MP3Utils {
    private static LRUCache<MP3, Bitmap> coverCache = new LRUCache<>(10);
    private static Bitmap DEFAULT_BITMAP;

    static {
        DEFAULT_BITMAP = BitmapFactory.decodeResource(
                ChorusApplication.getsApplicationContext().getResources(),
                R.drawable.marry
        );
    }

    public static Bitmap getAlbumart(MP3 mp3) {

        if (mp3 == null) {
            return null;
        }

        Bitmap albumArtBitMap = coverCache.get(mp3);

        if (albumArtBitMap != null) {
            return albumArtBitMap;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {

            Uri uri = Uri
                    .parse("content://media/external/audio/albumart/" + mp3.getAlbumId());

            ParcelFileDescriptor pfd = ChorusApplication.getsApplicationContext().getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                        options);
                pfd = null;
                fd = null;
            }

            if (albumArtBitMap == null) {
                albumArtBitMap = DEFAULT_BITMAP;
            }

            coverCache.put(mp3, albumArtBitMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return albumArtBitMap;
    }
}
