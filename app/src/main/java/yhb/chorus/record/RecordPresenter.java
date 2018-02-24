package yhb.chorus.record;

import android.content.Context;


/**
 * Created by yhb on 18-1-17.
 */

class RecordPresenter implements RecordContract.Presenter {
    private Context mContext;
    private RecordContract.View mView;


    RecordPresenter(Context context, RecordContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }


}
