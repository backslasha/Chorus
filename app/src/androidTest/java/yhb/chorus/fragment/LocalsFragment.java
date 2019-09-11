package yhb.chorus.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import yhb.chorus.adapter.LocalsListViewAdapter;
import yhb.chorus.service.MainService;
import yhb.chorus.utils.Utils;
import yhb.chorus.R;
import yhb.chorus.entity.MP3;


public class LocalsFragment extends Fragment {
    private List<MP3> mp3Beans;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_mp3s, null);
        ListView listView = (ListView) view.findViewById(R.id.lv_at_frag_mp3s_id);

        mp3Beans = Utils.getInstance(getActivity()).getLocals();

        LocalsListViewAdapter adapter = new LocalsListViewAdapter(getActivity(), mp3Beans);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utils.mp3Beans = mp3Beans;
                Intent intent;
                if (Utils.currentPosition == position) {
                    intent = new Intent(MainService.REMOTE_INTENT_PLAY_PAUSE);
                } else {
                    Utils.currentPosition = position;
                    intent = new Intent(MainService.ACTION_POINT);
                }
                getActivity().sendBroadcast(intent);
            }
        });
        return view;
    }


}
