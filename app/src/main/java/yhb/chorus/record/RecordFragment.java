package yhb.chorus.record;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import yhb.chorus.R;

/**
 * Created by yhb on 18-1-17.
 */

public class RecordFragment extends Fragment implements RecordContract.View, View.OnClickListener {

    private RecordContract.Presenter mPresenter;

    public static RecordFragment newInstance() {
        Bundle args = new Bundle();
        RecordFragment fragment = new RecordFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_record, container, false);
        bindViews(root);
        mPresenter.start();
        return root;
    }


    private FloatingActionButton mFloatingActionButton;
    private TextView mTextView;
    private boolean isRecording = false;

    private void bindViews(View root) {
        mFloatingActionButton = root.findViewById(R.id.float_action_button_record);
        mTextView = root.findViewById(R.id.text_view_record_state);

        mFloatingActionButton.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setPresenter(RecordContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.float_action_button_record:
                if (isRecording) {
                    RecordUtils.getInstance().stop();
                    mTextView.setText("STOP.");
                    isRecording = false;
                } else {
                    mTextView.setText("RECORDING...");
                    RecordUtils.getInstance().start();
                    isRecording = true;
                }
                break;
        }
    }


}

