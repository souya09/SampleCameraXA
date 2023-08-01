package jp.ac.hec.cm0107.samplecamerax;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    int mode = -1;
    public static final String EXTRA_DATA
            = "jp.ac.hec.cm0107.samplecamerax.DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spn = (Spinner)findViewById(R.id.spinner);
        String selected = (String)spn.getSelectedItem();

        if ( "NormalMode".equals(selected) ) {
            mode = 1;
        } else if ( "CharacterMode".equals(selected) ) {
            mode = 2;
        } else if ( "FrameMode".equals(selected) ) {
            mode = 3;
        }

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra(EXTRA_DATA, mode);
            startActivity(intent);
        });
    }
}