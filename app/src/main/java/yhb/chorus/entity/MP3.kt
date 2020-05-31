package yhb.chorus.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.litepal.annotation.Column
import org.litepal.crud.DataSupport

data class MP3(@Column(unique = true)
               private var _id: Long = 0,/* 音乐id MediaStore.Audio.Media._ID */
               var title: String? = null,/* 音乐标题 MediaStore.Audio.Media.TITLE */
               var artist: String? = null,/* 艺术家 MediaStore.Audio.Media.ARTIST */
               var duration: Int = 0,/* 时长 MediaStore.Audio.Media.DURATION = 0 */
               var size: Long = 0,/* 文件大小 MediaStore.Audio.Media.SIZE */
               @Column(unique = true) var uri: String? = null,/* 文件路径 MediaStore.Audio.Media.DATA */
               var album: String? = null, /* 唱片图片MediaStore.Audio.Media.ALBUM */
               var albumId: Long = 0,/* 唱片图片ID MediaStore.Audio.Media.ALBUM_ID */
               var isMusic: Int = 0 /* 是否为音乐 MediaStore.Audio.Media.IS_MUSIC = 0 */) : DataSupport(), Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readInt())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(_id)
        dest.writeString(title)
        dest.writeString(artist)
        dest.writeInt(duration)
        dest.writeLong(size)
        dest.writeString(uri)
        dest.writeString(album)
        dest.writeLong(albumId)
        dest.writeInt(isMusic)
    }

    fun readFromParcel(parcel: Parcel): MP3 {
        return MP3(parcel.readLong(), parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readInt())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MP3> {
        override fun createFromParcel(parcel: Parcel): MP3 {
            return MP3(parcel)
        }

        override fun newArray(size: Int): Array<MP3?> {
            return arrayOfNulls(size)
        }
    }

}