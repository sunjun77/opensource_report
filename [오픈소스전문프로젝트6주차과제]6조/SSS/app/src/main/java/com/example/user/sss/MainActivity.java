package com.example.user.sss;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private ImageButton buttonAnalyze;
    private ImageButton buttonSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonAnalyze = (ImageButton)findViewById(R.id.open_analyze);
        buttonAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnalyze();
            }
        });

        buttonSetting = (ImageButton)findViewById(R.id.open_setting);
        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSetting();
            }
        });
    }

    public void openAnalyze(){
        Intent intent = new Intent(this, analyze.class);
        startActivity(intent);
    }

    public void openSetting(){
        Intent intent = new Intent(this, setting.class);
        startActivity(intent);
    }
}
