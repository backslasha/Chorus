// ICallback.aidl
package yhb.chorus;

// Declare any non-default types here with import statements

interface ICallback {
    void onComplete();
    void onNewCurrent();
    void onNewRemoteIntent(String aciton);
}
