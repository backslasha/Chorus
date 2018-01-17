package yhb.chorus.entity;

public class LirDwonInfo {
    private int artist_id;
    private String UrlString;
    private String songName;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    private String artistName;


    public int getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(int artist_id) {
        this.artist_id = artist_id;
    }

    public String getUrlString() {
        return UrlString;
    }

    public void setUrlString(String urlString) {
        UrlString = urlString;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
