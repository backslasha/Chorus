package yhb.chorus.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.util.Log;

public class Mp3Scanner {
    private Context mContext;
    private SQLiteDatabase database;

    //首次运行创建一个带三张表格的数据库:hyson.db
    public Mp3Scanner(Context context) {
        this.mContext = context;
        this.database = new Mp3DatabaseOpenHelper(mContext, "hyson.db", null, 1).getWritableDatabase();
    }

    public void scanMediaStore() {
        database.execSQL("delete from locals");
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("artist", artist);
            values.put("uri", uri);
            values.put("duration", duration);
            values.put("albumId",albumId);
            database.insert("locals", null, values);
        }
        Log.d("haibiao", "扫描完毕，总共条" + cursor.getCount() + "数据");
        cursor.close();
    }


}