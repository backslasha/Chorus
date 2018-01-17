package yhb.chorus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import yhb.chorus.adapter.MainListViewBaseAdapter;
import yhb.chorus.R;


public class MainFragment extends Fragment {
    public interface MainInterface {
        void enterSecFrags(Fragment fragment, String fragmentName);
    }

    private String[] listNames = {"本地音乐", "我的最爱", "最近播放", "新建列表"};
    private MainInterface mainInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainInterface = (MainInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_main, null);


        BaseAdapter adapter = new MainListViewBaseAdapter(getActivity(), listNames);
        ListView listView = (ListView) view.findViewById(R.id.lv_at_main_id);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mainInterface.enterSecFrags(new LocalsFragment(), "本地音乐");
                        break;
                    case 1:
                        mainInterface.enterSecFrags(new FavouritesFragment(), "我的最爱");
                        break;
                }

            }
        });
        return view;
    }


}
