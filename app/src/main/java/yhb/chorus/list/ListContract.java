package yhb.chorus.list;

import java.util.ArrayList;
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

        void showLocalMP3s(List<MP3> mp3s);

        void turnOnEditable(boolean on);
    }

    interface Presenter extends BasePresenter {
        void release();

        /**
         * scan and collect all local mp3s, then create a database of them.
         */
        void scanMediaStoreAndCreateDB();


        void getLocalMP3s();

        void savedIntoQueue(ArrayList<MP3> selectedMP3s);

        void clearQueue();
    }
}
