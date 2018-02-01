package yhb.chorus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import yhb.chorus.entity.MP3;

import static yhb.chorus.db.MP3DbSchema.*;

/**
 * Created by yhb on 18-1-19.
 */

public class DBUtils {


    private <T> ArrayList<T> query(String tableName, Class<T> entityType, String fieldName, String value, Context context) {

        SQLiteDatabase database = new MP3DBHelper(context).getReadableDatabase();
        ArrayList<T> list = new ArrayList<>();
        Cursor cursor = database.query(tableName, null, fieldName + " like ?", new String[]{value}, null, null, " _id desc", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                T t = entityType.newInstance();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    Object content = null;
                    String columnName = cursor.getColumnName(i);// 获取数据记录第i条字段名的

                    switch (columnName) {
                        case MP3Table.Cols.ID:
                        case MP3Table.Cols.ALBUM_ID:
                        case MP3Table.Cols.SIZE:
                            content = cursor.getLong(cursor.getColumnIndex(columnName));
                            break;
                        case MP3Table.Cols.TITLE:
                        case MP3Table.Cols.ARTIST:
                        case MP3Table.Cols.URI:
                        case MP3Table.Cols.ALBUM:
                            content = cursor.getString(cursor.getColumnIndex(columnName));
                            break;
                        case MP3Table.Cols.IS_MUSIC:
                        case MP3Table.Cols.DURATION:
                            content = cursor.getInt(cursor.getColumnIndex(columnName));
                            break;
                    }
                    if (columnName.equals("_id")) {
                        columnName = "id";
                    }
                    Field field = entityType.getDeclaredField(columnName);//获取该字段名的Field对象。
                    field.setAccessible(true);//取消对age属性的修饰符的检查访问，以便为属性赋值
                    field.set(t, content);
                    field.setAccessible(false);//恢复对age属性的修饰符的检查访问
                }
                list.add(t);
                cursor.moveToNext();
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        return list;
    }

    private static <T> ArrayList<T> queryAllLocalMP3s(String tableName, Class<T> entityType, Context context) {

        SQLiteDatabase database = new MP3DBHelper(context).getReadableDatabase();

        ArrayList<T> list = new ArrayList<>();

        Cursor cursor = database.query(tableName, null, null, null, null, null, " " + MP3Table.Cols.ID + " desc", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                T t = entityType.newInstance();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    Object content = null;
                    String columnName = cursor.getColumnName(i);// 获取数据记录第i条字段名的

                    switch (columnName) {
                        case MP3Table.Cols.ID:
                        case MP3Table.Cols.ALBUM_ID:
                        case MP3Table.Cols.SIZE:
                            content = cursor.getLong(cursor.getColumnIndex(columnName));
                            break;
                        case MP3Table.Cols.TITLE:
                        case MP3Table.Cols.ARTIST:
                        case MP3Table.Cols.URI:
                        case MP3Table.Cols.ALBUM:
                            content = cursor.getString(cursor.getColumnIndex(columnName));
                            break;
                        case MP3Table.Cols.IS_MUSIC:
                        case MP3Table.Cols.DURATION:
                            content = cursor.getInt(cursor.getColumnIndex(columnName));
                            break;
                    }

                    if (columnName.equals("_id")) {
                        columnName = "id";
                    }
                    Field field = entityType.getDeclaredField(columnName);//获取该字段名的Field对象。
                    field.setAccessible(true);//取消对age属性的修饰符的检查访问，以便为属性赋值
                    field.set(t, content);
                    field.setAccessible(false);//恢复对age属性的修饰符的检查访问
                }
                list.add(t);
                cursor.moveToNext();
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        database.close();
        return list;
    }

    public static ArrayList<MP3> queryAllLocalMP3s(Context context) {

        SQLiteDatabase database = new MP3DBHelper(context).getReadableDatabase();

        ArrayList<MP3> list = new ArrayList<>();

        Cursor cursor = database.query(MP3Table.NAME, null, null, null, null, null, " " + MP3Table.Cols.ID + " desc", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                MP3 mp3 = new MP3();

                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    Object content = null;
                    String columnName = cursor.getColumnName(i);// 获取数据记录第i条字段名的

                    switch (columnName) {
                        case MP3Table.Cols.ID:
                        case MP3Table.Cols.ALBUM_ID:
                        case MP3Table.Cols.SIZE:
                            content = cursor.getLong(cursor.getColumnIndex(columnName));
                            break;
                        case MP3Table.Cols.TITLE:
                        case MP3Table.Cols.ARTIST:
                        case MP3Table.Cols.URI:
                        case MP3Table.Cols.ALBUM:
                            content = cursor.getString(cursor.getColumnIndex(columnName));
                            break;
                        case MP3Table.Cols.IS_MUSIC:
                        case MP3Table.Cols.DURATION:
                            content = cursor.getInt(cursor.getColumnIndex(columnName));
                            break;
                    }

                    if (columnName.equals("_id")) {
                        columnName = "id";
                    }
                    Field field = MP3.class.getDeclaredField(columnName);//获取该字段名的Field对象。
                    field.setAccessible(true);//取消对age属性的修饰符的检查访问，以便为属性赋值
                    field.set(mp3, content);
                    field.setAccessible(false);//恢复对age属性的修饰符的检查访问
                }
                list.add(mp3);
                cursor.moveToNext();
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        database.close();
        return list;
    }

    public static void scanMediaStoreAndCreateDB(Context context) {

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            SQLiteDatabase database = new MP3DBHelper(context).getReadableDatabase();
            database.execSQL("delete from " + "Queue");
            database.execSQL("delete from " + MP3Table.NAME);
            while (cursor.moveToNext()) {
                database.insert(MP3Table.NAME, null, getContentValues(getEntityFromMediaStoreCursor(cursor)));
            }
            database.close();
            cursor.close();
        }
    }

    public static List<Long> queryAllQueueMP3sId(Context context) {
        SQLiteDatabase database = new MP3DBHelper(context).getReadableDatabase();

        ArrayList<Long> list = new ArrayList<>();

        Cursor cursor = database.query(
                "Queue",
                null,
                null,
                null,
                null,
                null,
                " _id desc",
                null
        );

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            list.add(cursor.getLong(cursor.getColumnIndex("_id")));
            cursor.moveToNext();
        }

        cursor.close();
        database.close();
        return list;
    }

    public static void insertIntoQueue(ArrayList<MP3> selectedMP3s, Context context) {
        try (SQLiteDatabase database = new MP3DBHelper(context).getWritableDatabase()) {
            // insert into list_menu
            for (MP3 selectedMP3 : selectedMP3s) {
                database.insert("Queue", null, getContentValues(selectedMP3.getId()));
            }

        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFromQueue(ArrayList<MP3> selectedMP3s, Context context) {
        // delete from list_menu
        SQLiteDatabase database = new MP3DBHelper(context).getWritableDatabase();
        for (MP3 selectedMP3 : selectedMP3s) {
            database.delete("Queue", "_id=", new String[]{String.valueOf(selectedMP3.getId())});
        }
        database.close();
    }

    public static void deleteAllFromQueue(Context context) {
        // delete from list_menu
        SQLiteDatabase database = new MP3DBHelper(context).getWritableDatabase();
        database.delete("Queue", null, null);
        database.close();
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

    private static ContentValues getContentValues(long id) {
        ContentValues values = new ContentValues();
        values.put(MP3Table.Cols.ID, id);
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
}
