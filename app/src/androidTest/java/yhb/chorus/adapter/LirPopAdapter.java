package yhb.chorus.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import yhb.chorus.entity.LirDwonInfo;
import yhb.chorus.R;


public class LirPopAdapter extends BaseAdapter {
    private List<LirDwonInfo> mLirDwonInfos;
    private Context mContext;

    public LirPopAdapter(List<LirDwonInfo> lirDwonInfos,Context mContext) {
        this.mLirDwonInfos = lirDwonInfos;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mLirDwonInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mLirDwonInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) View.inflate(mContext, R.layout.item_simple_list_01,null);
        view.setText(mLirDwonInfos.get(position).getSongName()+"/"+mLirDwonInfos.get(position).getArtistName());
        return view;
    }
}
