package yhb.chorus.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

public class MP3 extends DataSupport implements Parcelable {

    public void setUri(String uri) {
        this.uri = uri;
    }

    private long _id; // 音乐id MediaStore.Audio.Media._ID

    private String title;// 音乐标题 MediaStore.Audio.Media.TITLE

    private String artist;// 艺术家 MediaStore.Audio.Media.ARTIST

    public MP3() {
    }

    public MP3(long _id, String title, String artist, int duration, long size, String uri, String album, long albumId, int isMusic) {
        this._id = _id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.size = size;
        this.uri = uri;
        this.album = album;
        this.albumId = albumId;
        this.isMusic = isMusic;
    }

    private int duration;// 时长 MediaStore.Audio.Media.DURATION

    private long size; // 文件大小 MediaStore.Audio.Media.SIZE

    private String uri;// 文件路径 MediaStore.Audio.Media.DATA

    private String album; // 唱片图片MediaStore.Audio.Media.ALBUM

    private long albumId;// 唱片图片ID MediaStore.Audio.Media.ALBUM_ID

    private int isMusic;// 是否为音乐 MediaStore.Audio.Media.IS_MUSIC

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUri() {
        return uri;
    }


    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getIsMusic() {
        return isMusic;
    }

    public void setIsMusic(int isMusic) {
        this.isMusic = isMusic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MP3 mp3 = (MP3) o;

        if (_id != mp3._id) return false;
        return uri.equals(mp3.uri);
    }

    @Override
    public int hashCode() {
        int result = (int) (_id ^ (_id >>> 32));
        result = 31 * result + uri.hashCode();
        return result;
    }

    /*
     * parcel
     *
     */

    public static final Creator<MP3> CREATOR = new Creator<MP3>() {
        @Override
        public MP3 createFromParcel(Parcel source) {
            return new MP3(source);
        }

        @Override
        public MP3[] newArray(int size) {
            return new MP3[size];
        }
    };

    public MP3(Parcel source) {
        readFromParcel(source);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeInt(duration);
        dest.writeLong(size);
        dest.writeString(uri);
        dest.writeString(album);
        dest.writeLong(albumId);
        dest.writeInt(isMusic);
    }

    public void readFromParcel(Parcel in) {
        _id = in.readLong();
        title = in.readString();
        artist = in.readString();
        duration = in.readInt();
        size = in.readLong();
        uri = in.readString();
        album = in.readString();
        albumId = in.readLong();
        isMusic = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
