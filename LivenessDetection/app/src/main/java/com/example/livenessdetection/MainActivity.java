package com.example.livenessdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    private String selected_result_single;
    private String selected_result_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner sp_select_single = (Spinner)findViewById(R.id.spinner);
        sp_select_single.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                selected_result_single = (String) sp_select_single.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner sp_select_group = (Spinner)findViewById(R.id.spinner2);
        sp_select_group.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                selected_result_group = (String) sp_select_group.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button bt_single = (Button)findViewById(R.id.button);
        bt_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_single = new Intent(MainActivity.this, SingleInputActivity.class);
                intent_single.putExtra("selected_item_single",selected_result_single);
                startActivity(intent_single);
            }
        });

        Button bt_group = (Button)findViewById(R.id.button2);
        bt_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_group = new Intent(MainActivity.this, GroupInputActivity.class);
                intent_group.putExtra("selected_item_group",selected_result_group);
                startActivity(intent_group);
            }
        });
    }
}