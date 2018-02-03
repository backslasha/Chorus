package yhb.chorus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import yhb.chorus.entity.MP3;
import yhb.chorus.utils.Utils;
import yhb.chorus.R;


public class FavouritesListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<MP3> mp3BeanList;

    public FavouritesListViewAdapter(Context context, List<MP3> mp3BeanList) {
        this.mContext = context;
        this.mp3BeanList = mp3BeanList;
    }

    @Override
    public int getCount() {
        return mp3BeanList.size();
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_mp3_simple, null);
        TextView tv = (TextView) view.findViewById(R.id.text_view_song_name);
        tv.setText((position + 1) + ". " + mp3BeanList.get(position).getTitle());
//        final ImageButton ibtn = (ImageButton) view.findViewById(R.id.image_button_favour);
//        ibtn.setBackgroundResource(R.drawable.ic_delete);

//        ibtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Utils.getInstance(mContext).updateBelongs(mp3BeanList.get(position), Utils.COLUMN_FAVOURITE, Utils.UPDATE_REMOVE);
//                mp3BeanList.get(position).setIsFavourite(0);
//                mp3BeanList.remove(position);
//                FavouritesListViewAdapter.this.notifyDataSetChanged();
//            }
//        });
        return view;
    }
}
