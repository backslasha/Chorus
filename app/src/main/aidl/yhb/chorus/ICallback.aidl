// ICallback.aidl
package yhb.chorus;

// Declare any non-default types here with import statements

interface ICallback {
    void onComplete();
    void onProgressChange(boolean isPlaying,int progress);
    void onNewRemoteIntent(String aciton);
}
