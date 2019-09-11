package yhb.chorus.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import yhb.chorus.fragment.ConsoleFragment;
import yhb.chorus.fragment.LiricFragment;
import yhb.chorus.fragment.MainFragment;
import yhb.chorus.service.MainService;
import yhb.chorus.utils.Mp3Scanner;
import yhb.chorus.utils.Utils;
import yhb.chorus.widgets.SlimSeekBar;
import yhb.chorus.R;


public class MainActivity extends BaseActivity implements MainFragment.MainInterface, ConsoleFragment.ConsoleInterface, LiricFragment.LiricInterface, View.OnClickListener {
    private Button playMode;
    private Toolbar toolbar;
    private SlimSeekBar mSlimSeekbar;
    private ImageButton addList;
    private TextView toolbarTitle;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private boolean isMain = true;
    private Utils utils;
    private SeekBarReceiver receiver;
    private String[] menuTitles = {"个人中心", "定时退出", "音乐闹钟", "主题切换", "关于我们"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("hyson", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirstIn", true)) {
            new Mp3Scanner(this).scanMediaStore();
        }

        //默认显示MainFragment
        fragmentManager = getSupportFragmentManager();
        MainFragment mainFragment = new MainFragment();
        transaction = fragmentManager.beginTransaction();
        transaction.commit();

//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_id);
//        toolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title_id);
//        addList = (ImageButton) findViewById(R.id.ibtn_add_id);
//        toolbar = (Toolbar) findViewById(R.id.toolbar_id);
//        mSlimSeekbar = (SlimSeekBar) findViewById(R.id.sb_id);
//
//        playMode = (Button) findViewById(R.id.btn_playmode_id);
        playMode.setOnClickListener(this);

        //toolbar菜单与抽屉建立联系
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(toggle);

        initdrawer();

        Intent serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);

        receiver = new SeekBarReceiver();
//        IntentFilter intentFilter = new IntentFilter(MainService.ACTION_RENEW_PROGRESS);
//        registerReceiver(receiver, intentFilter);

        mSlimSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
//                    Intent proChangeIntent = new Intent(MainService.ACTION_PROGRESS_CHANGE);
//                    proChangeIntent.putExtra("changeTo", progress);
//                    sendBroadcast(proChangeIntent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        utils = Utils.getInstance(this);

    }

    //MainFragment内部接口的回调方法，进入其他Fragment时需要做的一些准备
    @Override
    public void enterSecFrags(final Fragment fragment, String fragmentName) {
        isMain = false;
        transaction = fragmentManager.beginTransaction();
        if(fragmentName.equals("Hyson")){
            transaction.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
        }else {
            transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_out_to_left);
        }
//        transaction.replace(R.id.rl_container_id, fragment);
        transaction.commit();

        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace);
        addList.setVisibility(View.INVISIBLE);
        toolbarTitle.setText(fragmentName);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterSecFrags(new MainFragment(), "Hyson");
                initToolbar();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences("hyson", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFirstIn", false);
        editor.apply();

        unregisterReceiver(receiver);
    }

    //重写back键模拟碎片的返回栈
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && !isMain) {
            enterSecFrags(new MainFragment(), "Hyson");
            initToolbar();
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && isMain) {
            MainActivity.this.finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        String str = (String) playMode.getText();
        switch (str) {
            case "列表循环":
                Utils.getInstance(this).setPlayMode(Utils.MODE_SINGLE_LOOP);
                playMode.setText("单曲循环");
                break;
            case "单曲循环":
                Utils.getInstance(this).setPlayMode(Utils.MODE_ROADOM);
                playMode.setText("随机播放");
                break;
            case "随机播放":
                Utils.getInstance(this).setPlayMode(Utils.MODE_LIST_LOOP);
                playMode.setText("列表循环");
                break;
        }

    }

    //接收并调整进度条进度信息的广播
    private class SeekBarReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSlimSeekbar.setMax(Utils.currentMP3.getDuration());
            mSlimSeekbar.setProgress(intent.getIntExtra("curProgress", 0));
        }
    }

    //初始化toolbar
    private void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        addList.setVisibility(View.VISIBLE);
        fragmentManager.popBackStack();
        toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(toggle);
        isMain = true;
    }


    //初始化抽屉里层菜单
    private void initdrawer() {
        // Button button = (Button) findViewById(R.id.btn_exit_id);
        ListView listView = (ListView) findViewById(R.id.lv_drawer_id);
        // button.setOnClickListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.item_simple_list_01, menuTitles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, menuTitles[position] + ".", Toast.LENGTH_SHORT).show();
            }
        });

    }
}


