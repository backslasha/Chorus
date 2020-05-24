package yhb.chorus.main

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import yhb.chorus.R
import yhb.chorus.gank.GankActivity
import yhb.chorus.gank.GankFragment
import yhb.chorus.record.RecordActivity
import yhb.chorus.utils.ActivityUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mPresenter: MainPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            try {
                supportActionBar.title = getCountDown("2020-10-01")
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            window.statusBarColor = resources.getColor(android.R.color.transparent)
        }
        var mainFragment = supportFragmentManager
                .findFragmentById(R.id.container) as MainFragment?
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance()
            ActivityUtils.addFragmentToActivity(supportFragmentManager,
                    mainFragment, R.id.container)
        }
        mPresenter = MainPresenter(this, mainFragment)
        val checkStorage = ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)
        val checkAudio = ContextCompat.checkSelfPermission(this, permission.RECORD_AUDIO)
        if (PackageManager.PERMISSION_GRANTED != checkStorage || PackageManager.PERMISSION_GRANTED != checkAudio) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    permission.WRITE_EXTERNAL_STORAGE,
                    permission.RECORD_AUDIO
            ),
                    PERMISSION_REQUEST_CODE
            )
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
    }

    @Suppress("SameParameterValue")
    @Throws(ParseException::class)
    private fun getCountDown(dest: String): String {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.time = SimpleDateFormat("yy-MM-dd", Locale.CHINA).parse(dest)
        var delta = calendar.timeInMillis - today.time
        val days = (delta / 1000 / 60 / 60 / 24).toInt()
        delta -= days * 1000 * 60 * 60 * 24.toLong()
        val hours = (delta / 1000 / 60 / 60).toInt()
        delta -= hours * 1000 * 60 * 60.toLong()
        val minutes = (delta / 1000 / 60).toInt()
        return "还有 $days 天 $hours 小时 $minutes 分钟"
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_all -> {
                val intent = GankActivity.newIntent(this, GankFragment.TYPE_ALL)
                startActivity(intent)
            }
            R.id.nav_android -> {
                val intent = GankActivity.newIntent(this, GankFragment.TYPE_ANDROID)
                startActivity(intent)
            }
            R.id.nav_welfare -> {
                val intent = GankActivity.newIntent(this, GankFragment.TYPE_WELFARE)
                startActivity(intent)
            }
            R.id.nav_app -> {
                val intent = GankActivity.newIntent(this, GankFragment.TYPE_APP)
                startActivity(intent)
            }
            R.id.nav_share -> {
            }
            R.id.nav_send -> {
            }
            R.id.nav_micro -> {
                val intent = RecordActivity.newIntent(this)
                startActivity(intent)
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 501

        const val TAG = "MainActivity"

        @JvmStatic
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}