package yhb.chorus.gang;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;

import yhb.chorus.R;
import yhb.chorus.entity.MP3;
import yhb.chorus.entity.MP3InQueue;
import yhb.chorus.service.PlayCenter;

/**
 * Created by yhb on 18-1-17.
 */

class GangPresenter implements GangContract.Presenter {
    private Context mContext;
    private GangContract.View mView;


    GangPresenter(Context context, GangContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }


}
