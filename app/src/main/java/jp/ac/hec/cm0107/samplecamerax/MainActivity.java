package jp.ac.hec.cm0107.samplecamerax;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    int mode = -1;
    int item = -1;
    String str;
    public static final String EXTRA_DATA
            = "jp.ac.hec.cm0107.samplecamerax.DATA";
    public static final String EDIT_DATA
            = "edtData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spn = (Spinner)findViewById(R.id.spinner);
        EditText edt = findViewById(R.id.edtTxt);
        Spinner itemSpn = findViewById(R.id.spnItem);


        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            String selected = (String)spn.getSelectedItem();
            String selectItem = (String) itemSpn.getSelectedItem();


            if ( "NormalMode".equals(selected) ) {
                mode = 1;
            } else if ( "CharacterMode".equals(selected) ) {
                mode = 2;
                str = edt.getText().toString();
                intent.putExtra("EDIT_DATA",str);
                if ("フキダシ".equals(selectItem)){
                    item = 1;
                } else if ("クマノミ".equals(selectItem)) {
                    item = 2;
                } else if ("金魚".equals(selectItem)) {
                    item = 3;
                }
                intent.putExtra("ITEM_DATA",item);
            } else if ( "FrameMode".equals(selected) ) {
                mode = 3;

            }
            intent.putExtra(EXTRA_DATA, mode);
            startActivity(intent);
        });
    }
}