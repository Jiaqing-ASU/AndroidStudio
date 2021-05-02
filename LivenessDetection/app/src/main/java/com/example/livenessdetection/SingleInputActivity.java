package com.example.livenessdetection;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class SingleInputActivity extends AppCompatActivity {

    private TextView tv_single_preprossing;
    private TextView tv_single_inferring;

    private String selected_result_single;

    public static final int SERVERPORT = 8088;

    public static final String SERVER_IP = "10.0.2.2";
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    private TextView edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleinput);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            selected_result_single = bundle.getString("selected_item_single");
        }

        clientTextColor = ContextCompat.getColor(this, R.color.black);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.textView5);

        tv_single_preprossing = (TextView)findViewById(R.id.textView4);
        String txt_preprossing = "Preprossing Result Shown Here";
        tv_single_preprossing.setText(txt_preprossing);
        tv_single_preprossing.setMovementMethod(ScrollingMovementMethod.getInstance());

        tv_single_inferring = (TextView)findViewById(R.id.textView5);
        String txt_inferring = "Inferring Result Shown Here";
        tv_single_inferring.setText(txt_inferring);
        tv_single_inferring.setMovementMethod(ScrollingMovementMethod.getInstance());

        Button bt_single_pro = (Button)findViewById(R.id.button3);

        bt_single_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgList.removeAllViews();
                showMessage("Connecting to Server...", clientTextColor);
                clientThread = new ClientThread();
                thread = new Thread(clientThread);
                thread.start();
                showMessage("Connected to Server...", clientTextColor);
                return;
            }
        });

        Button bt_single_inf = (Button)findViewById(R.id.button4);
        bt_single_inf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clientMessage = selected_result_single;
                showMessage(clientMessage, Color.BLUE);
                if (null != clientThread) {
                    clientThread.sendMessage(clientMessage);
                }
            }
        });

        Button bt_single_return = (Button)findViewById(R.id.button5);
        bt_single_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_main = new Intent(SingleInputActivity.this, MainActivity.class);
                startActivity(intent_main);
            }
        });
    }

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = selected_result_single;
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
                msgList.addView(textView(message, color));
            }
        });
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