package yhb.chorus.main;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import yhb.chorus.R;
import yhb.chorus.gank.GankActivity;
import yhb.chorus.gank.GankFragment;
import yhb.chorus.record.RecordActivity;
import yhb.chorus.utils.ActivityUtils;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private MainPresenter mPresenter;
    public static final String TAG = "MainActivity";

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            try {
                supportActionBar.setTitle(getCountDown("2018-02-08"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        }

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    mainFragment, R.id.container);
        }

        mPresenter = new MainPresenter(this, mainFragment);

        int checkStorage = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int checkAudio = ContextCompat.checkSelfPermission(this, RECORD_AUDIO);
        if (PERMISSION_GRANTED != checkStorage || PERMISSION_GRANTED != checkAudio) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            WRITE_EXTERNAL_STORAGE,
                            RECORD_AUDIO
                    },
                    502
            );
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        toolbar.setNavigationIcon(R.drawable.ic_menu);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private String getCountDown(String dest) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        calendar.setTime(new SimpleDateFormat("yy-MM-dd").parse(dest));
        long delta = calendar.getTimeInMillis() - today.getTime();

        int days = (int) (delta / 1000 / 60 / 60 / 24);

        delta -= days * 1000 * 60 * 60 * 24;

        int hours = (int) (delta / 1000 / 60 / 60);

        delta -= hours * 1000 * 60 * 60;

        int minutes = (int) (delta / 1000 / 60);

        return "还有 " + days + " 天 " + hours + " 小时 " + minutes + " 分钟";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 502: {
                if (grantResults.length > 0
                        && grantResults[0] == PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_all) {
            Intent intent = GankActivity.newIntent(this, GankFragment.TYPE_ALL);
            startActivity(intent);
        } else if (id == R.id.nav_android) {
            Intent intent = GankActivity.newIntent(this, GankFragment.TYPE_ANDROID);
            startActivity(intent);
        } else if (id == R.id.nav_welfare) {
            Intent intent = GankActivity.newIntent(this, GankFragment.TYPE_WELFARE);
            startActivity(intent);
        } else if (id == R.id.nav_app) {
            Intent intent = GankActivity.newIntent(this, GankFragment.TYPE_APP);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_micro) {
            Intent intent = RecordActivity.newIntent(this);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
