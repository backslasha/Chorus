package yhb.chorus.main;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import yhb.chorus.R;
import yhb.chorus.db.MP3DBHelper;
import yhb.chorus.db.MP3DbSchema.MP3Table;
import yhb.chorus.entity.MP3;
import yhb.chorus.service.PlayCenter;

/**
 * Created by yhb on 18-1-17.
 */

class MainPresenter implements MainContract.Presenter {
    private final ContentObserver mSettingsContentObserver;
    private Context mContext;
    private MainContract.View mView;
    private PlayCenter mPlayCenter;
    private SQLiteDatabase mDatabase;
    private AudioManager mAudioManager = null;
    private int mVolumeSystem = -1, mVolumeSystemMax = -1;

    MainPresenter(Context context, MainContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
        mDatabase = new MP3DBHelper(context).getWritableDatabase();
        mPlayCenter = PlayCenter.getInstance(context);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSettingsContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                mVolumeSystem = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mView.invalidateSeekBarVolumeSystem(mVolumeSystem, mVolumeSystemMax);
                super.onChange(selfChange);
            }
        };
        mContext.getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                mSettingsContentObserver
        );
    }

    @Override
    public void start() {
        loadMP3sFromDB(mPlayCenter);
        mVolumeSystemMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumeSystem = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mView.invalidateSeekBarVolumeSystem(mVolumeSystem, mVolumeSystemMax);
    }

    @Override
    public void loadMP3sFromDB(final PlayCenter playCenter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playCenter.setMp3s(query(MP3Table.NAME, MP3.class, null, null));
            }
        }).start();
    }

    @Override
    public void loadSavedSetting() {
        // todo LoadSaveSetting
    }

    @Override
    public void setCurrentVolumeSystem(int volume) {
        mVolumeSystem = volume;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeSystem, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    @Override
    public void reloadCurrentWidgetsData(boolean needCover) {

        int progress = (int) (mPlayCenter.getVolume() * 10);
        int playMode = mPlayCenter.getPlayMode();
        Bitmap cover = null;
        String songName = "";
        String artistName = "";

        MP3 currentMP3 = mPlayCenter.getCurrentMP3();
        if (currentMP3 != null) {
            if (needCover) {
                cover = mPlayCenter.getAlbumart(currentMP3);
            }
            songName = currentMP3.getTitle();
            artistName = currentMP3.getArtist();
        }else {
            if (needCover) {
                cover = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.marry);
            }
            songName = mContext.getResources().getString(R.string.song_name);
            artistName = mContext.getResources().getString(R.string.artist_name);
        }

        mView.invalidateWidgets(progress, playMode, cover, songName, artistName);
    }

    @Override
    public void saveCurrentVolume(float volume) {
        mPlayCenter.recordVolume(volume);
    }

    @Override
    public void next() {
        mPlayCenter.next(true);
    }

    @Override
    public void playOrPause() {
        mPlayCenter.playOrPause();
    }

    @Override
    public void previous() {
        mPlayCenter.previous(true);
    }

    @Override
    public void nextPlayMode() {
        mPlayCenter.nextPlayMode();
    }

    @Override
    public List<MP3> loadQueueMP3s() {
        // todo loadQueueMP3s should not be simply load all data only.
        return mPlayCenter.getMp3s();
    }

    @Override
    public MP3 getCurrentMP3() {
        return mPlayCenter.getCurrentMP3();
    }

    @Override
    public void release() {
        mContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        mDatabase.close();
    }

    @Override
    public int getCurrentVolumeSystem() {
        return mVolumeSystem;
    }

    @Override
    public int getMaxVolumeSystem() {
        return mVolumeSystemMax;
    }

    @Override
    public void point(MP3 mp3) {
        mPlayCenter.point(mp3);
    }


    private <T> ArrayList<T> query(String tableName, Class<T> entityType, String fieldName, String value) {

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
                        columnName = columnName.replace("_", "");
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
                            content = cursor.getString(cursor.getColumnIndex(columnName));
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
}
