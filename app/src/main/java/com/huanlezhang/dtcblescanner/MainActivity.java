package com.huanlezhang.dtcblescanner;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BLE = 1;
    private static final int PERMISSION_REQUEST_ID = 2;

    private static final String[] PermissionStrings = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private ToggleButton mScanBtn;

    private RecyclerView mScanView;
    private MyRecyclerViewAdapter mScanViewAdapter;
    private RecyclerView.LayoutManager mScanViewLayoutManager;

    private HashMap<String, MyBleDeviceInfoStore> mScanResultMap = new HashMap<>();
    private ArrayList<MyBleDeviceInfoStore> mScanResultList = new ArrayList<>();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mBleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);

            if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                BluetoothDevice bluetoothDevice = result.getDevice();
                String deviceName = bluetoothDevice.getName();

                if (deviceName == null) {
                    deviceName = "Unknown";
                    return;
                }

                String deviceAddress = bluetoothDevice.getAddress();
                int rssi = result.getRssi();
                //Long timestamp = result.getTimestampNanos() / 1000000; // nano to mis
                Long timestamp = System.currentTimeMillis();

                MyBleDeviceInfoStore myBleDeviceInfoStore =
                        new MyBleDeviceInfoStore(deviceName, deviceAddress, rssi, timestamp);
                mScanResultMap.put(deviceAddress, myBleDeviceInfoStore);

                mScanViewAdapter.update(myBleDeviceInfoStore);

                // Log.d("DTC", deviceName + " " + deviceAddress + " " + rssi);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
            Log.d("DTC", "onBatch");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, PermissionStrings, PERMISSION_REQUEST_ID);
        }

        mScanBtn = findViewById(R.id.scanToggleBtn);
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleButton toggleButton = (ToggleButton) view;
                if (toggleButton.isChecked()) {
                    // on - scan

                    mScanResultMap.clear();
                    mScanViewAdapter.clear();

                    ScanSettings.Builder builder = new ScanSettings.Builder();
                    builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                    builder.setReportDelay(0);

                    mBluetoothLeScanner.startScan(null, builder.build(), mBleScanCallback);


                } else {
                    // off - stop scan
                    mBluetoothLeScanner.stopScan(mBleScanCallback);

                }
            }
        });

        mScanView = findViewById(R.id.scanView);
        mScanViewLayoutManager = new LinearLayoutManager(this);
        mScanView.setLayoutManager(mScanViewLayoutManager);

        mScanViewAdapter = new MyRecyclerViewAdapter(mScanResultList);
        mScanView.setAdapter(mScanViewAdapter);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BLE && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothManager = (BluetoothManager) this.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothManager == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBleItent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBleItent, REQUEST_ENABLE_BLE);
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

    }

}
