package yhb.chorus.list;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

import yhb.chorus.R;
import yhb.chorus.entity.MP3;
import yhb.chorus.service.PlayCenter;
import yhb.chorus.db.DBUtils;

/**
 * Created by yhb on 18-1-17.
 */

class ListPresenter implements ListContract.Presenter {
    private Context mContext;
    private ListContract.View mView;
    private PlayCenter mPlayCenter;

    ListPresenter(Context context, ListContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
        mPlayCenter = PlayCenter.getInstance(context);
    }

    @Override
    public void start() {
        getLocalMP3s();
    }

    @Override
    public void release() {

    }

    @Override
    public void scanMediaStoreAndCreateDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mView.showProgressBar();
                DBUtils.scanMediaStoreAndCreateDB(mContext);
                mView.hideProgressBar();
                getLocalMP3s();
            }
        }).start();
    }

    @Override
    public void getLocalMP3s() {
        mView.showLocalMP3s(DBUtils.queryAllLocalMP3s(mContext));
    }

    @Override
    public void savedIntoQueue(ArrayList<MP3> selectedMP3s) {

        // 去除重复添加到 PlayCenter(Memory)
        int size = mPlayCenter.addIntoQueue(selectedMP3s);

        Toast.makeText(mContext, "以添加" + size + "首歌到队列.", Toast.LENGTH_SHORT).show();

        // 去除重复添加到 database(Hard)
        DBUtils.insertIntoQueue(selectedMP3s, mContext);
    }


}
