package yhb.chorus.stub;

import yhb.chorus.BasePresenter;
import yhb.chorus.BaseView;

/**
 * Created by yhb on 18-1-17.
 */

public interface StubContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {

    }
}
