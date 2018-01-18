package yhb.chorus.main;

import java.util.List;

import yhb.chorus.BasePresenter;
import yhb.chorus.BaseView;
import yhb.chorus.entity.MP3;

/**
 * Created by yhb on 18-1-17.
 */

public interface MainContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {
    }
}
