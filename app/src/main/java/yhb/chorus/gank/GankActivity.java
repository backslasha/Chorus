package yhb.chorus.gank;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.fragment.app.Fragment;
import android.view.View;

import yhb.chorus.BaseFragmentActivity;

public class GankActivity extends BaseFragmentActivity {
    private GankPresenter mPresenter;

    public static final String TAG = "MainActivity";

    public static Intent newIntent(Context context, String type) {
        Intent intent = new Intent(context, GankActivity.class);
        intent.putExtra(GankFragment.ARGUE_TYPE, type);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return GankFragment.newInstance(getIntent().getStringExtra(GankFragment.ARGUE_TYPE));
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
        mPresenter = new GankPresenter(this, (GankContract.View) mFragment);
    }
}
