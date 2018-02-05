package yhb.chorus.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;

import static android.support.v4.util.Preconditions.checkNotNull;

/**
 * Created by yhb on 18-1-17.
 */

public class ActivityUtils {
    /**
     * The {@code mFragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     */
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics out = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(out);
        return out.heightPixels;
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics out = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(out);
        return out.widthPixels;
    }
}
