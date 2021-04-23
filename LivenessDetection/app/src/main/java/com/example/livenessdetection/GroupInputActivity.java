package com.example.livenessdetection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GroupInputActivity extends AppCompatActivity{

    private TextView tv_group_preprossing;
    private TextView tv_group_inferring;
    private File file;

    public void uploadVideo(View view) {
        Toast.makeText(getApplicationContext(), "Start Uploading the group of datasets", Toast.LENGTH_LONG).show();
        String base_url = "http://0.0.0.0:5000/";
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder().baseUrl(base_url).addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = retrofitBuilder.client(httpClient.build()).build();
        UploadHelper fileUtils = retrofit.create(UploadHelper.class);
        String desc = "one group of datasets";
        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), desc);
        RequestBody requestFile = RequestBody.create(MediaType.parse("text/plain"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Call<ResponseBody> call = fileUtils.uploadVideo(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(response.toString());
                Log.e("mw", "Server_response: " + response.toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println(t.toString());
                Log.e("mw", "upload_err: " + t.toString());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupinput);

        tv_group_preprossing = (TextView)findViewById(R.id.textView7);
        String txt_preprossing = "Preprossing Result Shown Here";
        tv_group_preprossing.setText(txt_preprossing);
        tv_group_preprossing.setMovementMethod(ScrollingMovementMethod.getInstance());

        tv_group_inferring = (TextView)findViewById(R.id.textView8);
        String txt_inferring = "Inferring Result Shown Here";
        tv_group_inferring.setText(txt_inferring);
        tv_group_inferring.setMovementMethod(ScrollingMovementMethod.getInstance());

        Button bt_group_pro = (Button)findViewById(R.id.button6);
        bt_group_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_group_preprossing.setText("Preprossing Working");
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_group_preprossing.setText("Preprossing Finished");
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });

        Button bt_group_inf = (Button)findViewById(R.id.button7);
        bt_group_inf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_group_inferring.setText("Inferring Working");
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_group_inferring.setText("Inferring Finished\nHere is the results:\nAccuracy:\nFalse accept rate:\nFalse reject rate:\nHalf total error:\nF1 score:");
                                try {
                                    Thread.sleep(15000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });

        Button bt_group_return = (Button)findViewById(R.id.button8);
        bt_group_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_main = new Intent(GroupInputActivity.this, MainActivity.class);
                startActivity(intent_main);
            }
        });
    }
}