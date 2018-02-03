// IMyAidlInterface.aidl
package yhb.chorus;


import yhb.chorus.entity.MP3;
import yhb.chorus.ICallback;
// Declare any non-default types here with import statements

interface IPlayer {

    void playOrPause( inout MP3 mp3) ;

    void next( inout MP3 mp3) ;

    void previous( inout MP3 mp3) ;

    void point( inout MP3 mp3) ;

    void setVolume(int progress);

    void seekTo(int progress);

    void registerCallback(ICallback callback);
}
