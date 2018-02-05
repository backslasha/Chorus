package yhb.chorus.utils;

import java.lang.reflect.Type;

import okhttp3.OkHttpClient;

/**
 * Created by yhb on 18-2-4.
 */

public class RequestManager {
    public interface HttpListener {

        void onSuccess(Object result);

        void onFailure(int errorType, String message);
    }


    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    public static void getList(Object tag, final String url, final Type type, final boolean isCache, final HttpListener listener) {

    }
}


