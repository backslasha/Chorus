package yhb.chorus.gank;

import android.content.Context;

import java.util.List;

import yhb.chorus.entity.response.GanHuo;
import yhb.chorus.entity.response.GankResponse;
import yhb.chorus.http.OkHttpUtils;

/**
 * Created by yhb on 18-1-17.
 */

class GankPresenter implements GankContract.Presenter {

    private Context mContext;
    private GankContract.View mView;
    private int mPage = 0;

    GankPresenter(Context context, GankContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        loadNewPageAsync();
    }

    @Override
    public void loadNewPageAsync() {
        loadPageAsync(++mPage);
    }

    private void loadPageAsync(int page) {
        OkHttpUtils.doRequest(
                getUrl(mView.getType(), 20, page),
                GankResponse.class,
                new OkHttpUtils.HttpResponseListener<GankResponse>() {

                    @Override
                    public void onSuccess(GankResponse result) {
                        if (result.getError()) {
                            return;
                        }

                        List<GanHuo> ganHuoList = result.getGanHuos();
                        if (ganHuoList != null) {
                            mView.showNewPage(ganHuoList);
                        }
                    }

                    @Override
                    public void onFailure(int errorType, String message) {

                    }
                }
        );
    }

    private String getUrl(String type, int pageSize, int page) {
        return "http://gank.io/api/data/" + type + "/"
                + String.valueOf(pageSize) + "/"
                + String.valueOf(page);
    }


}
