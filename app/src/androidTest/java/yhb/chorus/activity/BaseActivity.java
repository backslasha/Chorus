package yhb.chorus.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;


public class BaseActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showToast(String toast){
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }
}
