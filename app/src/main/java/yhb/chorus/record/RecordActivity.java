package yhb.chorus.record;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import yhb.chorus.BaseFragmentActivity;
import yhb.chorus.R;

public class RecordActivity  extends BaseFragmentActivity {
    private RecordPresenter mPresenter;

    public static final String TAG = "MainActivity";

    public static Intent newIntent(Context context) {
        return new Intent(context, RecordActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return RecordFragment.newInstance();
    }

    @Override
    protected void beforeCreate() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
            }
        }

    }

    @Override
    protected void afterCreate() {
        super.afterCreate();
        mPresenter = new RecordPresenter(this, (RecordContract.View) mFragment);
    }
}
