package yhb.chorus.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import yhb.chorus.adapter.LirPopAdapter;
import yhb.chorus.entity.LirBean;
import yhb.chorus.entity.LirDwonInfo;
import yhb.chorus.service.MainService;
import yhb.chorus.utils.LirBuilder;
import yhb.chorus.utils.Utils;
import yhb.chorus.widgets.LirView;
import yhb.chorus.R;

public class LiricFragment extends Fragment {
    private View view;
    private LirView lirView;
    private LirBuilder lirBuilder;
    private View popView;
    private LiricReceiver receiver;
    private View dialogView;
    private List<LirBean> lirics;
    private AlertDialog.Builder builder;

    public interface LiricInterface {
        void enterSecFrags(Fragment fragment, String fragmentName);
    }

    private LiricInterface mainInterface;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_liric, container, false);
        popView = inflater.inflate(R.layout.item_pop, null);
        lirBuilder = new LirBuilder(getActivity());
        lirView = (LirView) view.findViewById(R.id.liric_view_id);

        lirView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        lirics = lirBuilder.getLirFromLocal();
        lirView.setLirics(lirics);

//        Bitmap bitmap = Utils.getInstance(getActivity()).getAlbumart(Utils.currentMp3Bean);
//        Drawable drawable = new BitmapDrawable(bitmap);
//        if(bitmap!=null){
//            lirView.setBackground(drawable);

//        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        receiver = new LiricReceiver();
        IntentFilter intentFilter = new IntentFilter(MainService.ACTION_NEXT);
        intentFilter.addAction(MainService.ACTION_POINT);
        intentFilter.addAction(MainService.ACTION_PREVIOUS);
        intentFilter.addAction(MainService.ACTION_RENEW_PROGRESS);
        context.registerReceiver(receiver, intentFilter);

        mainInterface = (LiricInterface) context;
    }

    //准备好dialog，设置搜索按钮的监听事件，点击时启动异步任务显示歌词条目
    private void showDialog() {
        dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.item_dialog, null);
        Button btn = (Button) dialogView.findViewById(R.id.btn_at_dialog_id);
        final EditText edt = (EditText) dialogView.findViewById(R.id.edt_at_dialog_id);
        edt.setText(Utils.getInstance(getActivity()).mp3Beans.get(Utils.currentPosition).getTitle());

        builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.show();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LirTitlesTask().execute(edt.getText().toString());
                dialog.dismiss();
            }
        });


    }

    //在子线程中获取歌词条目，在主线程中准备好popWindow，用来显示歌词条目，并设置好listView监听事件
    private class LirTitlesTask extends AsyncTask<String, Void, List<LirDwonInfo>> {

        @Override
        protected List<LirDwonInfo> doInBackground(String... params) {
            List<LirDwonInfo> liricTitles = new ArrayList<>();
            try {
                JSONObject jsonO = new JSONObject(lirBuilder.getAllLirsJsonMsgFromHttp(params[0]));
                JSONArray jsonA = jsonO.getJSONArray("result");
                for (int i = 0; i < jsonA.length(); i++) {
                    LirDwonInfo info = new LirDwonInfo();

                    jsonO = jsonA.getJSONObject(i);
                    info.setArtist_id(jsonO.getInt("artist_id"));
                    info.setSongName(jsonO.getString("song"));
                    info.setUrlString(jsonO.getString("lrc"));

                    jsonO = new JSONObject(lirBuilder.getArtistJsonMsgWithId(jsonO.getInt("artist_id")));
                    jsonO = jsonO.getJSONObject("result");
                    info.setArtistName(jsonO.getString("name"));

                    liricTitles.add(info);
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
            return liricTitles;
        }

        @Override
        protected void onPostExecute(final List<LirDwonInfo> lirDwonInfos) {
            final PopupWindow popupWindow = new PopupWindow((int) (view.getWidth() * 0.9f), (int) (view.getHeight() * 0.6f));

            ListView listView = (ListView) popView.findViewById(R.id.lv_at_pop_id);

            LirPopAdapter adapter = new LirPopAdapter(lirDwonInfos, getActivity());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LirDwonInfo lirDwonInfo = lirDwonInfos.get(position);
                    new LirDwonLoadTask().execute(lirDwonInfo.getUrlString());
                    popupWindow.dismiss();
                }
            });
            popupWindow.setContentView(popView);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);

            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.showAtLocation(lirView, Gravity.CENTER, 0, 0);
        }
    }

    //点击pop中歌词条目连接启动的歌词下载任务
    private class LirDwonLoadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                //打开http输入流获取数据
                InputStream in = conn.getInputStream();
                InputStreamReader inr = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(inr);

                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line + "\n");
                }
                reader.close();
                Log.d("haibiao", "read: " + str.toString());

                //打开本地lrc文件的输出流，保存从输入流获得的歌词文件
                FileOutputStream out = getActivity().openFileOutput(Utils.mp3Beans.get(Utils.currentPosition).getTitle().replace(".mp3", ".lrc"), Context.MODE_PRIVATE);
                OutputStreamWriter outw = new OutputStreamWriter(out);
                BufferedWriter writer = new BufferedWriter(outw);

                writer.write(str.toString());
                Log.d("haibiao", "write:" + str.toString());
                writer.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("haibiao", "error:" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mainInterface.enterSecFrags(new LiricFragment(), "歌词");
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    //广播接收器
    private class LiricReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            lirics = new LirBuilder(getActivity()).getLirFromLocal();
            switch (intent.getAction()) {
                case MainService.ACTION_RENEW_PROGRESS:
                    lirView.setTime(intent.getIntExtra("curProgress", 0));
                    for (int i = 0; i < lirics.size(); i++) {
                        if (lirView.getTime() <= lirics.get(i).getTime()) {
                            lirView.setIndex(i - 1);
                            break;
                        }
                    }
                    lirView.invalidate();
                    Log.d("haibiao", "invalidate:" + lirics.size());
                    break;
                default:
                    lirView.setLirics(lirics);
                    break;
            }


        }
    }

}