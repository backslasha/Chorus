package yhb.chorus.http;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import yhb.chorus.app.ChorusApplication;
import yhb.chorus.utils.NetUtils;

/**
 * Created by yhb on 18-2-4.
 */

public class OkHttpUtils {

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    private static Handler handler = new Handler(Looper.getMainLooper());

    public interface HttpResponseListener<T> {

        void onSuccess(T result);

        void onFailure(int errorType, String message);
    }

    public static void doRequest(String url, Class entity, final HttpResponseListener listener) {
        doRequest(null, url, entity, listener);
    }

    public static void doRequest(Object tag, String url, final Class entity, final HttpResponseListener listener) {

        if (!NetUtils.isConnected(ChorusApplication.getsApplicationContext())) {
            listener.onFailure(5, "网络开小差了！！");
            return;
        }

        if (!NetUtils.isNetAvailable(ChorusApplication.getsApplicationContext())) {
            listener.onFailure(5, "当前网络不可用！！");
            return;
        }

        Request request = new Request.Builder()
                .tag(tag)
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (!call.isCanceled()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(4, e.getLocalizedMessage());
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String json = response.body().string();
                if (!call.isCanceled()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess(
                                    new Gson().fromJson(json, entity)
                            );
                        }
                    });

                }
            }
        });
    }

    public static void cancelRequest(Object tag) {
        for (Call call : okHttpClient.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(tag)) {
                call.cancel();
            }
        }
        for (Call call : okHttpClient.dispatcher().runningCalls()) {
            if (call.request().tag().equals(tag)) {
                call.cancel();
            }
        }
    }
}


