package com.example.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothList extends AppCompatActivity {

    private static final String TAG = "BluetoothList";
    public static String EXTRA_DEVICE_ADRESS = "device_address";
    ListView idList;

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairDeviesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);
        idList = findViewById(R.id.idList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Verification();
        mPairDeviesArrayAdapter = new ArrayAdapter(this, R.layout.list);
        idList = findViewById(R.id.idList);
        idList.setAdapter(mPairDeviesArrayAdapter);
        idList.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size()>0){
            for(BluetoothDevice device:pairedDevices){
                mPairDeviesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3){
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length()-17);
            finishAffinity();
            Intent intent = new Intent(BluetoothList.this, MainActivity.class);
            intent.putExtra(EXTRA_DEVICE_ADRESS, address);
            startActivity(intent);
        }
    };

    private void Verification(){
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null){
            Toast.makeText(getBaseContext(),"Bluetooth not found", Toast.LENGTH_SHORT).show();
        } else {
            if(mBtAdapter.isEnabled()){
                Log.d(TAG,"Active bluetooth");
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}