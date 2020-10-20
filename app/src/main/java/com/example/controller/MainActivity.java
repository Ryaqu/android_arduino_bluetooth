package com.example.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    ImageButton btn_play, btn_forward, btn_left, btn_right, btn_stop, btn_down;
    TextView textView;
    Button btn_disconnect;

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIn = new StringBuilder();
    private ConnectedThread MyConnectionBt;
    private static final UUID BTMODUL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if(msg.what == handlerState) {
                    char MyChar = (char) msg.obj;
                    if (MyChar == 'w') {
                        textView.setText("Forward");
                    }
                    if (MyChar == 'a') {
                        textView.setText("Left");
                    }
                    if (MyChar == 's') {
                        textView.setText("Back");
                    }
                    if (MyChar == 'd') {
                        textView.setText("Right");
                    }
                    if (MyChar == 'f') {
                        textView.setText("STOP");
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Verification();
        editText = findViewById(R.id.textField);
        btn_play = findViewById(R.id.btn_play);
        btn_forward = findViewById(R.id.btn_forward);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);
        btn_stop = findViewById(R.id.btn_stop);
        btn_down = findViewById(R.id.btn_down);

        btn_disconnect = findViewById(R.id.btn_disconnect);

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getData = editText.getText().toString();
                MyConnectionBt.write(getData);
            }
        });

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket != null) {
                    try {btSocket.close();}
                    catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            }
        });

        btn_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConnectionBt.write("w");
            }
        });

        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConnectionBt.write("a");
            }
        });

        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConnectionBt.write("d");
            }
        });

        btn_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConnectionBt.write("s");
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConnectionBt.write("f");
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODUL);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(BluetoothList.EXTRA_DEVICE_ADRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e){
            Toast.makeText(getBaseContext(), "Problem with the socket", Toast.LENGTH_LONG);
        }
        try {
            btSocket.connect();
        } catch (IOException e){
            try {
                btSocket.close();
            } catch (IOException e2){

            }
        }
        MyConnectionBt = new ConnectedThread(btSocket);
        MyConnectionBt.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            btSocket.close();
        } catch (IOException e2){

        }
    }

    private void Verification(){
        if(btAdapter == null) {
            Toast.makeText(getBaseContext(), "Problem with the adapter", Toast.LENGTH_SHORT);
        } else {
            if(btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e){

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] byte_in = new byte[1];
            while(true){
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e){
                    break;
                }
            }
        }

        public void write(String input) {
            try{
                mmOutStream.write(input.getBytes());
            } catch (IOException e){
                Toast.makeText(getBaseContext(),"Problem with the context", Toast.LENGTH_LONG);
                finish();
            }
        }
    }
}