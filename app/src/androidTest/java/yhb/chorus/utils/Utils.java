package yhb.chorus.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import yhb.chorus.service.MainService;
import yhb.chorus.entity.MP3;


public class Utils {

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    private Context mContext;
    private SQLiteDatabase mDatabase;
    private static Utils utils;
    private static final String DATABASE_NAME = "hyson.db";
    public static int currentPosition = 0;
    private static int playMode = 2;
    public static List<MP3> mp3Beans;
    public static MP3 currentMP3;//缓存当前MP3

    public static final String COLUMN_FAVOURITE = "isFavourite";
    public static final String COLUMN_RECENT = "isRecent";
    public static final String COLUMN_LOCAL = "isLocal";

    public static final int UPDATE_ADD = 1;
    public static final int UPDATE_REMOVE = 0;
    public static final int MODE_SINGLE_LOOP = 1;
    public static final int MODE_LIST_LOOP = 2;
    public static final int MODE_ROADOM = 3;

    private Utils(Context context) {
        mContext = context;
        Mp3DatabaseOpenHelper mp3DatabaseOpenHelper = new Mp3DatabaseOpenHelper(mContext, DATABASE_NAME, null, 1);
        mDatabase = mp3DatabaseOpenHelper.getWritableDatabase();
    }

    public static Utils getInstance(Context context) {

        if (utils == null) {
            utils = new Utils(context);
        }
        return utils;
    }

    public void updateBelongs(MP3 mp3Bean, String which, int change) {
        ContentValues values = new ContentValues();
        values.put(which, change);
        mDatabase.update("locals", values, "uri = ?", new String[]{mp3Bean.getUri()});
        Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
    }

    public List<MP3> getLocals() {
        Cursor cursor = mDatabase.rawQuery("select * from locals where isLocal like 1", null);
        return getListFromCursor(cursor);
    }

    public List<MP3> getRecents() {
        Cursor cursor = mDatabase.rawQuery("select * from locals where isRecent like 1", null);
        return getListFromCursor(cursor);
    }

    public List<MP3> getFavours() {
        Cursor cursor = mDatabase.rawQuery("select * from locals where isFavourite like 1", null);
        return getListFromCursor(cursor);
    }

    private List<MP3> getListFromCursor(Cursor cursor) {
        List<MP3> list = new ArrayList<>();
        do {
            try {
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                String uri = cursor.getString(cursor.getColumnIndex("uri"));
                int albumId = cursor.getInt(cursor.getColumnIndex("albumId"));
                int duration = cursor.getInt(cursor.getColumnIndex("duration"));
                int isFavourite = cursor.getInt(cursor.getColumnIndex("isFavourite"));
                int isLocal = cursor.getInt(cursor.getColumnIndex("isLocal"));
                int isRecent = cursor.getInt(cursor.getColumnIndex("isRecent"));

                MP3 bean = new MP3();
                bean.setUri(uri);
                bean.setTitle(title);
                bean.setAlbumId(albumId);
                bean.setDuration(duration);
                bean.setArtist(artist);
                bean.setIsFavourite(isFavourite);
                bean.setIsLocal(isLocal);
                bean.setIsRecent(isRecent);

                if (isLocal == 0) {
                    continue;
                }
                list.add(bean);
            } catch (CursorIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        } while (cursor.moveToNext());
        Log.d("haibiao", "本地数据库扫描完毕，总共条" + cursor.getCount() + "数据,cursor已关闭");
        cursor.close();

        return list;
    }

    public void next() {
        setNextPathByMode();
        Intent intent = new Intent(MainService.ACTION_NEXT);
        mContext.sendBroadcast(intent);
    }

    public void previous() {
        setPreviousPathByMode();
        Intent intent = new Intent(MainService.ACTION_PREVIOUS);
        mContext.sendBroadcast(intent);
    }

    public void playOrPause() {
        Intent intent = new Intent(MainService.ACTION_PLAY_PAUSE);
        mContext.sendBroadcast(intent);
    }

    public void setPreviousPathByMode() {
        if (playMode == Utils.MODE_LIST_LOOP) {
            if (Utils.currentPosition > 0) {
                Utils.currentPosition -= 1;
            } else {
                Utils.currentPosition = Utils.mp3Beans.size() - 1;
            }
        } else if (playMode == Utils.MODE_ROADOM) {
            Utils.currentPosition = (int) (Utils.mp3Beans.size() * Math.random());
        } else if (playMode == Utils.MODE_SINGLE_LOOP) {

        }
    }

    public void setNextPathByMode() {
        if (playMode == Utils.MODE_LIST_LOOP) {
            if (Utils.currentPosition + 1 <= Utils.mp3Beans.size() - 1) {
                Utils.currentPosition += 1;
            } else {
                Utils.currentPosition = 0;
            }
        } else if (playMode == Utils.MODE_ROADOM) {
            Utils.currentPosition = (int) (Utils.mp3Beans.size() * Math.random());
        } else if (playMode == Utils.MODE_SINGLE_LOOP) {

        }
    }


    public Bitmap getAlbumart(MP3 mp3Bean) {
        Bitmap albumArtBitMap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {

            Uri uri = Uri
                    .parse("content://media/external/audio/albumart/" + mp3Beans.get(currentPosition).getAlbumId());

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


}
