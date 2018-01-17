package yhb.chorus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import yhb.chorus.entity.MP3;
import yhb.chorus.utils.Utils;
import yhb.chorus.R;


public class MainListViewBaseAdapter extends BaseAdapter {
    private Context mContext;
    private String[] listNames;

    public MainListViewBaseAdapter(Context context, String[] stringList) {
        this.mContext = context;
        this.listNames = stringList;
    }

    @Override
    public int getCount() {
        return listNames.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_main, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_at_main_item_id);

        List<MP3> tempList = null ;

        switch (listNames[position]){
            case "本地音乐":
                tempList = Utils.getInstance(mContext).getLocals();textView.setText(listNames[position]+"/"+tempList.size()+"首");
                break;
            case "我的最爱":
                tempList = Utils.getInstance(mContext).getFavours();textView.setText(listNames[position]+"/"+tempList.size()+"首");
                break;
            case "最近播放" :
                tempList = Utils.getInstance(mContext).getRecents();textView.setText(listNames[position]+"/"+tempList.size()+"首");
                break;
            case "新建列表" :
                textView.setText(listNames[position]);
                break;
            default:
                break;
        }


//        if (Utils.getInstance(mContext).mp3Beans.get(position).equals(tempList)) {
//            return view;
//        }
//        final Drawable playSmall = mContext.getResources().getDrawable(R.drawable.ic_play_circle_small);
//        final Drawable pauseSmall = mContext.getResources().getDrawable(R.drawable.ic_pause_circle_small);
//
//        final ImageButton imageButton = (ImageButton) view.findViewById(R.id.ibtn_item_local_list_id);
//        imageButton.setVisibility(View.VISIBLE);
//        imageButton.setBackground(playSmall);
//        imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (imageButton.getBackground() == pauseSmall) {
//                    imageButton.setBackground(playSmall);
//                } else if (imageButton.getBackground() == playSmall) {
//                    imageButton.setBackground(pauseSmall);
//                }
//                Toast.makeText(mContext, "the " + position + 1 + " imageButton clicked!", Toast.LENGTH_SHORT).show();
//            }
//        });
        return view;
    }
}
