package yhb.chorus.gank;

import java.util.List;

import yhb.chorus.BasePresenter;
import yhb.chorus.BaseView;
import yhb.chorus.entity.response.GanHuo;

/**
 * Created by yhb on 18-1-17.
 */

public interface GankContract {

    interface View extends BaseView<Presenter> {
        void showNewPage(List<GanHuo> ganHuoList);

        String getType();
    }

    interface Presenter extends BasePresenter {

        void loadNewPageAsync();
    }
}
