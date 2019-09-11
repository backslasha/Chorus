package yhb.chorus.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Mp3DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String CREATE_LOCALS = "create table locals (id integer primary key autoincrement,title text,artist text,duration integer,uri text,isLocal integer,albumId integer,isRecent integer,isFavourite integer)";
  //  private static final String CREATE_FAVOURS = "create table favours (id integer primary key autoincrement,title text,artist text,duration integer,uri text,isRencent integer)";
  //  private static final String CREATE_RECENTS = "create table recents (id integer primary key autoincrement,title text,artist text,duration integer,uri text,isFavour integer)";

    public Mp3DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCALS);
      //  db.execSQL(CREATE_FAVOURS);
      //  db.execSQL(CREATE_RECENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
