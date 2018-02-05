package yhb.chorus.gang;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import yhb.chorus.BaseFragmentActivity;
import yhb.chorus.R;

public class GangActivity extends BaseFragmentActivity {
    private GangPresenter mPresenter;

    public static final String TAG = "MainActivity";

    public static Intent newIntent(Context context) {
        return new Intent(context, GangActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return GangFragment.newInstance();
    }

    @Override
    protected void beforeCreate() {
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
        mPresenter = new GangPresenter(this, (GangContract.View) mFragment);
    }
}
