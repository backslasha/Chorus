package yhb.chorus.list;

import android.content.Context;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.SaveCallback;
import org.litepal.crud.callback.UpdateOrDeleteCallback;

import java.util.ArrayList;

import yhb.chorus.db.DBUtils;
import yhb.chorus.entity.MP3;
import yhb.chorus.entity.MP3InQueue;
import yhb.chorus.service.PlayCenter;

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
        mPlayCenter = PlayCenter.getInstance();
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
                clearQueue();
                mView.hideProgressBar();
                getLocalMP3s();
            }
        }).start();
    }

    @Override
    public void getLocalMP3s() {
        mView.showLocalMP3s(DataSupport.findAll(MP3.class));
    }

    @Override
    public void savedIntoQueue(ArrayList<MP3> selectedMP3s) {

        mView.showProgressBar();

        ArrayList<MP3InQueue> mp3InQueues = new ArrayList<>();
        for (MP3 selectedMP3 : selectedMP3s) {
            mp3InQueues.add(new MP3InQueue(selectedMP3));
        }

        // 去除重复添加到 PlayCenter(Memory)
        final int size = mPlayCenter.addIntoQueue(selectedMP3s);

        // 去除重复添加到 database(Hard)
        DataSupport.saveAllAsync(mp3InQueues).listen(new SaveCallback() {
            @Override
            public void onFinish(boolean success) {
                if (success) {

                    mView.hideProgressBar();

                    mView.turnOnEditable(false);

                    Toast.makeText(mContext, "以添加" + size + "首歌到队列.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void clearQueue() {
        mView.showProgressBar();

        DataSupport.deleteAllAsync(MP3InQueue.class.getSimpleName())
                .listen(new UpdateOrDeleteCallback() {
                    @Override
                    public void onFinish(int rowsAffected) {
                        mPlayCenter.setQueueMP3s(new ArrayList<MP3>());
                        Toast.makeText(mContext, "播放队列已清空.", Toast.LENGTH_SHORT).show();
                        mView.hideProgressBar();
                    }
                });


    }


}
