package yhb.chorus.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import yhb.chorus.service.MainService;
import yhb.chorus.utils.Utils;
import yhb.chorus.R;


public class ConsoleFragment extends Fragment implements View.OnClickListener {
    private Button play_pause, next, previous;
    private TextView songTitle, songDur, songCur;
    private ImageView artistPho;
    private LinearLayout console;
    private ConsoleReceiver receiver;

    public interface ConsoleInterface {
        void enterSecFrags(Fragment fragment, String fragmentName);
    }

    private ConsoleInterface mainInterface;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_console, null);

        play_pause = (Button) view.findViewById(R.id.btn_play_pause_Id);
        next = (Button) view.findViewById(R.id.btn_next_Id);
        previous = (Button) view.findViewById(R.id.btn_previous_Id);
        songTitle = (TextView) view.findViewById(R.id.tv_song_title_Id);
        songCur = (TextView) view.findViewById(R.id.tv_song_current_progress_Id);
        songDur = (TextView) view.findViewById(R.id.tv_song_duration_Id);
        artistPho = (ImageView) view.findViewById(R.id.iv_artist_photo_id);
        console = (LinearLayout) view.findViewById(R.id.ll_console_id);

        play_pause.setOnClickListener(this);
        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        console.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        Intent serIntent = new Intent(getActivity(), MainService.class);
        getActivity().startService(serIntent);
        switch (v.getId()) {
            case R.id.btn_play_pause_Id:
                Utils.getInstance(getActivity()).playOrPause();
                break;
            case R.id.btn_next_Id:
                Utils.getInstance(getActivity()).next();
                break;
            case R.id.btn_previous_Id:
                Utils.getInstance(getActivity()).previous();
                break;
            case R.id.ll_console_id:
                mainInterface.enterSecFrags(new LiricFragment(), "歌词");
                Toast.makeText(getActivity(), "进入歌词界面", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Utils.mp3Beans == null) {
            Utils.mp3Beans = Utils.getInstance(getActivity()).getLocals();
            if (Utils.mp3Beans.size() > 0) {
                Utils.currentMP3 = Utils.mp3Beans.get(Utils.currentPosition);
            }
        }
        receiver = new ConsoleReceiver();
        IntentFilter intentFilter = new IntentFilter(MainService.ACTION_RENEW_PROGRESS);
        intentFilter.addAction(MainService.ACTION_CHANGE_FINISH);
        context.registerReceiver(receiver, intentFilter);

        mainInterface = (ConsoleInterface) context;
    }

    private class ConsoleReceiver extends BroadcastReceiver {
        Bitmap bitmap;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                case MainService.ACTION_CHANGE_FINISH:
                    if ((bitmap = Utils.getInstance(getActivity()).getAlbumart(Utils.currentMP3)) != null) {
                        artistPho.setImageBitmap(bitmap);
                    } else {
                        artistPho.setImageResource(R.drawable.marry);
                    }
                    songTitle.setText(Utils.currentMP3.getTitle());
                    songDur.setText(mm2min(Utils.currentMP3.getDuration()));
                    break;
                default:
                    songCur.setText(mm2min(intent.getIntExtra("curProgress", 0)) + "/");
                    if (intent.getBooleanExtra("isPlaying", false)) {
                        play_pause.setBackgroundResource(R.drawable.ic_pause_circle_outline);
                    } else {
                        play_pause.setBackgroundResource(R.drawable.ic_play_circle_outline);
                    }
                    break;
            }


        }
    }

    public String mm2min(int mm) {
        String min, sec;
        if (mm / 1000 / 60 > 9) {
            min = mm / 1000 / 60 + "";
        } else {
            min = "0" + mm / 1000 / 60;
        }

        if (((mm / 1000) % 60) <= 9) {
            sec = "0" + (mm / 1000) % 60;
        } else {
            sec = (mm / 1000) % 60 + "";
        }
        return min + ":" + sec;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }
}