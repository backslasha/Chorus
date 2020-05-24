package yhb.chorus.list;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import yhb.chorus.R;
import yhb.chorus.utils.ActivityUtils;

/**
 * Created by yhb on 18-1-17.
 */
public class ListActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private ListPresenter mPresenter;
    private ListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        listFragment = (ListFragment) getSupportFragmentManager()
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
    public void onBackPressed() {
        if (listFragment.isEditable()) {
            listFragment.turnOnEditable(false);
        } else {
            super.onBackPressed();
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
