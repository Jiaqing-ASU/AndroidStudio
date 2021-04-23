package com.example.livenessdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt_single = (Button)findViewById(R.id.button);
        bt_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_single = new Intent(MainActivity.this, SingleInputActivity.class);
                startActivity(intent_single);
            }
        });

        Button bt_group = (Button)findViewById(R.id.button2);
        bt_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_group = new Intent(MainActivity.this, GroupInputActivity.class);
                startActivity(intent_group);
            }
        });
    }
}