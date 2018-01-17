package yhb.chorus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;

import yhb.chorus.db.MP3DbSchema.MP3Table;

import static yhb.chorus.main.MainActivity.TAG;

/**
 * Created by yhb on 18-1-17.
 */

public class MP3DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "chorus_mp3.db";

    public MP3DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + MP3Table.NAME + "(" +
                "_id integer primary key autoincrement, " +
                MP3Table.Cols.ID + "," +
                MP3Table.Cols.TITLE + "," +
                MP3Table.Cols.ARTIST + "," +
                MP3Table.Cols.DURATION + "," +
                MP3Table.Cols.SIZE + "," +
                MP3Table.Cols.URI + "," +
                MP3Table.Cols.ALBUM + "," +
                MP3Table.Cols.ALBUM_ID + "," +
                MP3Table.Cols.IS_MUSIC +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
