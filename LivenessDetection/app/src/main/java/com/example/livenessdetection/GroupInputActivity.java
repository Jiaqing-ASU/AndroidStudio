package com.example.livenessdetection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GroupInputActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tv_group_preprossing;
    private TextView tv_group_inferring;

    private String selected_result_group;

    public static final int SERVERPORT = 8088;

    public static final String SERVER_IP = "10.0.2.2";
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgListG;
    private Handler handler;
    private int clientTextColor;
    private TextView edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupinput);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            selected_result_group = bundle.getString("selected_item_group");
        }

        clientTextColor = ContextCompat.getColor(this, R.color.black);
        handler = new Handler();
        msgListG = findViewById(R.id.msgListG);
        edMessage = findViewById(R.id.textView8);

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
                                tv_group_inferring.setText("Inferring Finished");
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

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "0";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message + " [" + getTime() + "]");
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgListG.addView(textView(message, color));
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button6) {
            msgListG.removeAllViews();
            showMessage("Connecting to Server...", clientTextColor);
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            showMessage("Connected to Server...", clientTextColor);
            return;
        }

        if (view.getId() == R.id.button7) {
            String clientMessage = edMessage.getText().toString().trim();
            showMessage(clientMessage, Color.BLUE);
            if (null != clientThread) {
                clientThread.sendMessage(clientMessage);
            }
        }
    }

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);

                while (!Thread.currentThread().isInterrupted()) {

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    if (null == message || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Server Disconnected.";
                        showMessage(message, Color.RED);
                        break;
                    }
                    showMessage("Server: " + message, clientTextColor);
                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != socket) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
    }
}