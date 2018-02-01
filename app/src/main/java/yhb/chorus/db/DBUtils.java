package yhb.chorus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.UpdateOrDeleteCallback;

import java.util.ArrayList;

import yhb.chorus.entity.MP3;


/**
 * Created by yhb on 18-1-19.
 */

public class DBUtils {


    private <T> ArrayList<T> query(String tableName, Class<T> entityType, String fieldName, String value, Context context) {
        return (ArrayList<T>) DataSupport.where(fieldName + " like ?", value).find(entityType);
    }

    private static <T> ArrayList<T> queryAllLocalMP3s(String tableName, Class<T> entityType, Context context) {
        return (ArrayList<T>) DataSupport.findAll(MP3.class);
    }

    public static void deleteFromQueue(ArrayList<MP3> selectedMP3s) {
        for (MP3 selectedMP3 : selectedMP3s) {
            selectedMP3.deleteAsync().listen(new UpdateOrDeleteCallback() {
                @Override
                public void onFinish(int rowsAffected) {

                }
            });
        }
    }

    public static void scanMediaStoreAndCreateDB(Context context) {

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            SQLiteDatabase database = LitePal.getDatabase();
            database.execSQL("delete from " + "MP3InQueue");
            database.execSQL("delete from " + MP3.class.getSimpleName());
            while (cursor.moveToNext()) {
                database.insert(MP3.class.getSimpleName(), null, getContentValues(getEntityFromMediaStoreCursor(cursor)));
            }
            database.close();
            cursor.close();
        }
    }

    private static ContentValues getContentValues(MP3 mp3) {
        ContentValues values = new ContentValues();
        values.put(MP3Table.Cols.TITLE, mp3.getTitle());
        values.put(MP3Table.Cols.ALBUM, mp3.getAlbum());
        values.put(MP3Table.Cols.ALBUM_ID, mp3.getAlbumId());
        values.put(MP3Table.Cols.ARTIST, mp3.getArtist());
        values.put(MP3Table.Cols.DURATION, mp3.getDuration());
        values.put(MP3Table.Cols.IS_MUSIC, mp3.getIsMusic());
        values.put(MP3Table.Cols.URI, mp3.getUri());
        values.put(MP3Table.Cols.SIZE, mp3.getSize());
        return values;
    }

    private static MP3 getEntityFromMediaStoreCursor(Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
        return new MP3(0, title, artist, duration, size, uri, album, albumId, isMusic);
    }

    static final class MP3Table {
        public static final String NAME = "mp3";

        static final class Cols {
            static final String ID = "_id";
            static final String TITLE = "title";
            static final String ARTIST = "artist";
            static final String DURATION = "duration";
            static final String SIZE = "size";
            static final String URI = "uri";
            static final String ALBUM = "album";
            static final String ALBUM_ID = "albumId";
            static final String IS_MUSIC = "isMusic";
        }
    }
}
