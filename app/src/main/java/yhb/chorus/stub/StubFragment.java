package yhb.chorus.stub;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import yhb.chorus.R;

/**
 * Created by yhb on 18-1-17.
 */

public class StubFragment extends Fragment implements StubContract.View, View.OnClickListener {

    private StubContract.Presenter mPresenter;

    public static StubFragment newInstance() {
        Bundle args = new Bundle();
        StubFragment fragment = new StubFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        bindViews(root);
        mPresenter.start();
        return root;
    }


    private void bindViews(View root) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setPresenter(StubContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_button_next:
                break;
        }
    }


}

