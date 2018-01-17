package yhb.chorus.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;

import yhb.chorus.db.MP3DBHelper;
import yhb.chorus.db.MP3DbSchema.MP3Table;
import yhb.chorus.entity.MP3;

import static yhb.chorus.db.MP3DbSchema.MP3Table.Cols.*;
import static yhb.chorus.main.MainActivity.TAG;

/**
 * Created by yhb on 18-1-17.
 */

class MainPresenter implements MainContract.Presenter {
    private Context mContext;
    private MainContract.View mView;
    private SQLiteDatabase mDatabase;

    MainPresenter(Context context, MainContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
        mDatabase = new MP3DBHelper(context).getWritableDatabase();
    }

    @Override
    public void start() {

    }

    @Override
    public void collectLocalMP3s() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mView.showProgressBar();
                scanMediaStore();
                mView.hideProgressBar();
            }
        }).start();
    }

    @Override
    public void getLocalMP3s() {
        ArrayList<MP3> mp3s = query(MP3Table.NAME, MP3.class, null, null);
        mView.showSongList(mp3s);
    }

    private  <T> ArrayList<T> query(String tableName, Class<T> entityType, String fieldName, String value) {

        ArrayList<T> list = new ArrayList<>();
        Cursor cursor;
        if (fieldName == null) {
            cursor = mDatabase.query(tableName, null, null, null, null, null, " id desc", null);
        } else {
            cursor = mDatabase.query(tableName, null, fieldName + " like ?", new String[]{value}, null, null, " id desc", null);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                T t = entityType.newInstance();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    Object content = null;
                    String columnName = cursor.getColumnName(i);// 获取数据记录第i条字段名的
                    if (columnName.equals("_id")) {
                        columnName = columnName.replace("_","" );
                    }
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
                            content= cursor.getString(cursor.getColumnIndex(columnName));
                            break;
                        case MP3Table.Cols.IS_MUSIC:
                        case MP3Table.Cols.DURATION:
                            content = cursor.getInt(cursor.getColumnIndex(columnName));
                            break;
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

    private void scanMediaStore() {
        mDatabase.execSQL("delete from " + MP3Table.NAME);
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

            ContentValues values = new ContentValues();
            values.put(TITLE, title);
            values.put(ARTIST, artist);
            values.put(DURATION, duration);
            values.put(SIZE, size);
            values.put(URI, uri);
            values.put(ALBUM, album);
            values.put(ALBUM_ID, albumId);
            values.put(IS_MUSIC, isMusic);

            mDatabase.insert(MP3Table.NAME, null, values);
        }
        Log.d(TAG, "扫描完毕，总共条" + cursor.getCount() + "数据");

        cursor.close();
    }
}
