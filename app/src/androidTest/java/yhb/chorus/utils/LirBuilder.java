package yhb.chorus.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yhb.chorus.entity.LirBean;


public class LirBuilder {

    private Context mContext;

    public LirBuilder(Context mContext) {
        this.mContext = mContext;
    }

    //从本地文件夹中找到并解析歌词返回歌词代表List
    public List<LirBean> getLirFromLocal() {
        List<LirBean> lirics = new ArrayList<>();
        try {
            FileInputStream fileInputStream = mContext.openFileInput(Utils.mp3Beans.get(Utils.currentPosition).getTitle().replace(".mp3", ".lrc"));
            InputStreamReader reader = new InputStreamReader(fileInputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String s;

            while ((s = bufferedReader.readLine()) != null) {

                s = s.replace("[", "");
                s = s.replace("]", "#");

                String splitLiricData[] = s.split("#");


                if (splitLiricData.length == 2) {
                    LirBean lirBean = new LirBean();
                    lirBean.setLiric(splitLiricData[1]);
                    lirBean.setTime(acquireLiricTime(splitLiricData[0]));
                    lirics.add(lirBean);
                } else if (splitLiricData.length == 3) {
                    LirBean lirBean1 = new LirBean();
                    LirBean lirBean2 = new LirBean();
                    lirBean1.setLiric(splitLiricData[2]);
                    lirBean2.setLiric(splitLiricData[2]);
                    lirBean1.setTime(acquireLiricTime(splitLiricData[0]));
                    lirBean2.setTime(acquireLiricTime(splitLiricData[1]));
                    lirics.add(lirBean1);
                    lirics.add(lirBean2);
                } else if (splitLiricData.length == 4) {
                    LirBean lirBean1 = new LirBean();
                    LirBean lirBean2 = new LirBean();
                    LirBean lirBean3 = new LirBean();
                    lirBean1.setLiric(splitLiricData[3]);
                    lirBean2.setLiric(splitLiricData[3]);
                    lirBean3.setLiric(splitLiricData[3]);
                    lirBean1.setTime(acquireLiricTime(splitLiricData[0]));
                    lirBean2.setTime(acquireLiricTime(splitLiricData[1]));
                    lirBean3.setTime(acquireLiricTime(splitLiricData[2]));
                    lirics.add(lirBean1);
                    lirics.add(lirBean2);
                    lirics.add(lirBean3);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(lirics);
        return lirics;
    }

    //从http获取歌词的json数据
    public String getAllLirsJsonMsgFromHttp(String songName) throws MalformedURLException, IOException {
        songName = URLEncoder.encode(songName, "utf-8").replaceAll("\\+", "%20");
        songName = songName.replaceAll("%3A", ":").replaceAll("%2F", "/");

        URL url = new URL("http://geci.me/api/lyric/" + songName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        InputStream in = conn.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inr);

        StringBuilder str = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            str.append(line);
        }
        reader.close();
        conn.disconnect();
        Log.d("TAG", "getLirTitlesFromHttp: " + str.toString());
        return str.toString();
    }

    //根据artist_id从http获取歌曲名
    public String getArtistJsonMsgWithId(int artist_id) throws MalformedURLException, IOException {
        URL url = new URL("http://geci.me/api/artist/" + artist_id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        InputStream in = conn.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inr);

        StringBuilder str = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            str.append(line);
        }
        reader.close();
        return str.toString();
    }

    // 将时间格式由“00:05.22”转换为int毫秒
    private int acquireLiricTime(String primTime) {
        int minute;
        int millisecond;
        int second;
        int currentTime;
        primTime = primTime.replace(":", ".");
        primTime = primTime.replace(".", "#");

        String primTimeData[] = primTime.split("#");

        try {
            minute = Integer.parseInt(primTimeData[0]);
            second = Integer.parseInt(primTimeData[1]);
            millisecond = Integer.parseInt(primTimeData[2]);

            // 时间转换为毫秒数
            currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
        } catch (NumberFormatException n) {
            return 0;
        }
        return currentTime;
    }


}
