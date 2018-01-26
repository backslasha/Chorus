package yhb.chorus.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import yhb.chorus.db.MP3DbSchema.MP3Table;

/**
 * Created by yhb on 18-1-17.
 */

public class MP3DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "chorus.db";

    public MP3DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + MP3Table.NAME + "(" +
                MP3Table.Cols.ID + " integer primary key autoincrement, " +
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
        db.execSQL("create table Queue (" + MP3Table.Cols.ID + " integer primary key)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



}
