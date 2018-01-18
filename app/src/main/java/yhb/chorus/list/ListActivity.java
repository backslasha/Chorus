package yhb.chorus.list;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import yhb.chorus.R;
import yhb.chorus.main.MainFragment;
import yhb.chorus.utils.ActivityUtils;

/**
 * Created by yhb on 18-1-17.
 */
public class ListActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private ListPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
//        }

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        ListFragment listFragment = (ListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        if (listFragment == null) {
            listFragment = ListFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    listFragment, R.id.container);
        }

        mPresenter = new ListPresenter(this, listFragment);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    502);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 502: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show();
                } else {

                }
            }
        }
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ListActivity.class);
        return intent;
    }
}
