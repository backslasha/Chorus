package yhb.chorus;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

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