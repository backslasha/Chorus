package yhb.chorus.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import yhb.chorus.R;
import yhb.chorus.common.SimpleAdapter;
import yhb.chorus.common.SimpleHolder;
import yhb.chorus.entity.MP3;

/**
 * Created by yhb on 18-1-17.
 */

public class MainFragment extends Fragment implements MainContract.View {

    private MainContract.Presenter mPresenter;
    private RecyclerView mRecyclerViewSongs;
    private AppCompatSeekBar mSeekBar;
    private SimpleAdapter<MP3> mMP3SimpleAdapter;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        bindViews(root);
        mPresenter.start();
        return root;
    }

    private void bindViews(View root) {
        mSeekBar = root.findViewById(R.id.seek_bar);
        mRecyclerViewSongs = root.findViewById(R.id.recycler_songs);
        mRecyclerViewSongs.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMP3SimpleAdapter = new SimpleAdapter<MP3>(getActivity(), R.layout.item_mp3) {
            @Override
            public void forEachHolder(SimpleHolder holder, MP3 mp3) {
                TextView textView = holder.getView(R.id.text_view_song_name);
                textView.setText(String.format("%s / %s", mp3.getTitle(), mp3.getArtist()));
            }
        };
        mRecyclerViewSongs.setAdapter(mMP3SimpleAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.drawer_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_1:
                mPresenter.collectLocalMP3s();
                break;
            case R.id.item_2:
                mPresenter.getLocalMP3s();
                break;
            default:

                break;
        }
        return true;
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void showProgressBar() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void showSongList(List<MP3> mp3s) {
        mMP3SimpleAdapter.performDataChanged(mp3s);
    }

}
