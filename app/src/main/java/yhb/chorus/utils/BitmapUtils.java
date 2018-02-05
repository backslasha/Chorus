package yhb.chorus.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.util.LruCache;

import java.io.FileDescriptor;
import java.io.IOException;

import yhb.chorus.R;
import yhb.chorus.app.ChorusApplication;
import yhb.chorus.entity.MP3;

/**
 * Created by yhb on 18-2-3.
 */

public class BitmapUtils {
    private static LruCache<MP3, Bitmap> coverCache = null;

    static {
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        coverCache = new LruCache<MP3, Bitmap>(maxMemory / 8) {
            @Override
            protected int sizeOf(MP3 key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
    }

    public static Bitmap getAlbumart(MP3 mp3, int reqWidth, int reqHeight) {

        if (mp3 == null) {
            return null;
        }

        Bitmap albumArtBitMap = coverCache.get(mp3);

        if (albumArtBitMap != null) {
            return albumArtBitMap;
        }

        ParcelFileDescriptor pfd = null;

        try {

            Uri uri = Uri
                    .parse("content://media/external/audio/albumart/" + mp3.getAlbumId());

            pfd = ChorusApplication.getsApplicationContext().getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = decodeSampledBitmapFromFileDescriptor(fd, reqWidth, reqHeight);
            }

            if (albumArtBitMap == null) {
                albumArtBitMap = decodeSampledBitmapFromResource(
                        ChorusApplication.getsApplicationContext().getResources(),
                        R.drawable.marry,
                        reqWidth,
                        reqHeight
                );
            }

            coverCache.put(mp3, albumArtBitMap);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pfd != null) {
                    pfd.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return albumArtBitMap;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources resource, int resId, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(resource, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(resource, resId);
    }

    public static Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        int halfWidth = width / 2;
        int halfHeight = height / 2;

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2;
        }

        return inSampleSize;
    }
}
