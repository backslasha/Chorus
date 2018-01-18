package yhb.chorus.list;

import java.util.List;

import yhb.chorus.BasePresenter;
import yhb.chorus.BaseView;
import yhb.chorus.entity.MP3;

/**
 * Created by yhb on 18-1-17.
 */

public interface ListContract {

    interface View extends BaseView<Presenter> {

        void showProgressBar();

        void hideProgressBar();

        void showSongList(List<MP3> mp3s);
    }

    interface Presenter extends BasePresenter {
        /**
         * scan and collect all local mp3s, then create a database of them.
         */
        void collectLocalMP3s();


        void getLocalMP3s();
    }
}
