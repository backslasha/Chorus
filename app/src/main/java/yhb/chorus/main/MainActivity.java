package yhb.chorus.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import yhb.chorus.BuildConfig;
import yhb.chorus.R;
import yhb.chorus.service.MainService;
import yhb.chorus.utils.ActivityUtils;

/**
 * Created by yhb on 18-1-17.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private MainPresenter mPresenter;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {

            try {
                Calendar calendar = Calendar.getInstance();
                Date today = calendar.getTime();

                calendar.setTime(new SimpleDateFormat("yy-MM-dd").parse("2018-02-08"));
                long delta = calendar.getTimeInMillis() - today.getTime();

                int days = (int) (delta / 1000 / 60 / 60 / 24);

                delta -= days * 1000 * 60 * 60 * 24;

                int hours = (int) (delta / 1000 / 60 / 60);

                delta -= hours * 1000 * 60 * 60;

                int minutes = (int) (delta / 1000 / 60);

                supportActionBar.setTitle("还有 " + days + " 天 " + hours + " 小时 " + minutes + " 分钟");

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    mainFragment, R.id.container);
        }

        mPresenter = new MainPresenter(this, mainFragment);


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
                } else {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }
}
