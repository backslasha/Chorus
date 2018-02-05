package yhb.chorus.stub;

import android.content.Context;

/**
 * Created by yhb on 18-1-17.
 */

class StubPresenter implements StubContract.Presenter {
    private Context mContext;
    private StubContract.View mView;


    StubPresenter(Context context, StubContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }


}
