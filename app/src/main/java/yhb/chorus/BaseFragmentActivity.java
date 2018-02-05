package yhb.chorus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by yhb on 18-1-17.
 */

public abstract class BaseFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    protected Fragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        beforeCreate();

        FragmentManager fm = getSupportFragmentManager();
        mFragment = fm.findFragmentById(R.id.container);
        if (mFragment == null) {
            mFragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }

        afterCreate();
    }


    protected void beforeCreate() {

    }

    protected void afterCreate() {

    }
}