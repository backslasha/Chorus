package yhb.chorus.app;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by yhb on 18-2-1.
 */

public class ChorusApplication extends Application {

    private static Context sApplicationContext;
    @Override
    public void onCreate() {
        super.onCreate();
        sApplicationContext = getApplicationContext();
        LitePal.initialize(sApplicationContext);
    }

    public static Context getsApplicationContext() {
        return sApplicationContext;
    }
}
