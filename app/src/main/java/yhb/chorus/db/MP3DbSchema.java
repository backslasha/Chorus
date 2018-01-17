package yhb.chorus.db;

/**
 * Created by yhb on 18-1-17.
 */

public class MP3DbSchema {
    public static final class MP3Table {
        public static final String NAME = "mp3";

        public static final class Cols {
            public static final String ID = "id";
            public static final String TITLE = "title";
            public static final String ARTIST = "artist";
            public static final String DURATION = "duration";
            public static final String SIZE = "size";
            public static final String URI = "uri";
            public static final String ALBUM = "album";
            public static final String ALBUM_ID = "albumId";
            public static final String IS_MUSIC = "isMusic";
        }
    }
}
