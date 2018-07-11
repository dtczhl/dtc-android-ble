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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BLE = 1;
    private static final int PERMISSION_REQUEST_ID = 2;

    private static final String[] PermissionStrings = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private Handler mHandler = new Handler();


    private FileOutputStream mFos;
    private File mMainDir;

    private CheckBox mLogDataCheckBox;
    private boolean mIsLogging = false;

    // rssi plot related
    private ImageView mRssiImageView;
    private int mRssiImageViewHeight = 0;
    private int mRssiImageViewWidth = 0;
    private Bitmap mRssiBitmap;
    private Canvas mRssiCanvas;
    private Paint mBaseLinePaint;
    private HashMap<String, Integer> mDeviceToPosition = new HashMap<>();
    private HashMap<String, MyPlotData> mPlotDataMap = new HashMap<>();
    private Timer mPlotTimer;
    private TimerTask mPlotTimerTask;
    private final int PLOT_TIMER_PERIOD = 200;
    private int mMaxRssi = -30;
    private int mMinRssi = -90;
    private ArrayList<Paint> mPaintColorList = new ArrayList<>();

    private TextView mImageViewLabelTop;
    private TextView mImageViewLabelMid;
    private TextView mImageViewLabelBtm;


    private ToggleButton mScanBtn;
    private TextView mInfoTextView;
    private EditText mTagEditText;

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
                Long timestamp = System.currentTimeMillis();

                MyBleDeviceInfoStore myBleDeviceInfoStore =
                        new MyBleDeviceInfoStore(deviceName, deviceAddress, rssi, timestamp);

                int position = mScanViewAdapter.update(myBleDeviceInfoStore);
                mDeviceToPosition.put(deviceAddress, position);


                if (mPlotDataMap.get(deviceAddress) == null) {
                    MyPlotData myPlotData = new MyPlotData(deviceAddress);
                    mPlotDataMap.put(deviceAddress, myPlotData);
                }

                if (mIsLogging) {
                    if (mScanViewAdapter.mCheckBoxArray.get(position, false)) { // checked item
                        String logData = deviceName + "," + deviceAddress + "," + Long.toString(timestamp) + "," + Integer.toString(rssi) + '\n';
                        try {
                            mFos.write(logData.getBytes());
                            mFos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mPlotDataMap.get(deviceAddress).add(rssi);
                    }
                }

                // Log.d("DTC", deviceName + " " + deviceAddress + " " + rssi);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
            Log.d("DTC", "!!!!!!!!!!!!!!!! onBatch");
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

                    findViewById(R.id.clearBtn).setEnabled(false);
                    mInfoTextView.setText("Scanning...");

                    mLogDataCheckBox.setEnabled(true);

                    mTagEditText.setEnabled(false);
                    String filename = mTagEditText.getText().toString().trim();
                    filename = filename + ".csv";

                    File file = new File(mMainDir, filename);

                    try {
                        mFos = new FileOutputStream(file, false);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    mPlotTimer = new Timer();
                    mPlotTimerTask = new TimerTask() {
                        @Override
                        public void run() {

                            for (MyPlotData myPlotData: mPlotDataMap.values()){
                                myPlotData.click();
                            }

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    drawRssi();
                                }
                            });
                        }
                    };
                    mPlotTimer.schedule(mPlotTimerTask, 0, PLOT_TIMER_PERIOD);

                    ScanSettings.Builder builder = new ScanSettings.Builder();
                    builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                    builder.setReportDelay(0);

                    mBluetoothLeScanner.startScan(null, builder.build(), mBleScanCallback);

                } else {
                    // off - stop scan

                    mPlotTimer.cancel();

                    findViewById(R.id.clearBtn).setEnabled(true);
                    mTagEditText.setEnabled(true);


                    mLogDataCheckBox.setEnabled(false);
                    mLogDataCheckBox.setChecked(false);

                    mIsLogging = false;

                    mInfoTextView.setText("Waiting...");

                    mBluetoothLeScanner.stopScan(mBleScanCallback);

                    try {
                        mFos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mInfoTextView = findViewById(R.id.infoTextView);
        mTagEditText = findViewById(R.id.tagEditView);

        mLogDataCheckBox = findViewById(R.id.logDataCheckBox);
        mLogDataCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    mIsLogging = true;
                } else {
                    mIsLogging = false;
                }
            }
        });

        mScanView = findViewById(R.id.scanView);
        mScanViewLayoutManager = new LinearLayoutManager(this);
        mScanView.setLayoutManager(mScanViewLayoutManager);

        mScanViewAdapter = new MyRecyclerViewAdapter(mScanResultList);
        mScanView.setAdapter(mScanViewAdapter);


        build_paintColorList();

        mImageViewLabelTop = findViewById(R.id.rssiViewLabelTop);
        mImageViewLabelMid = findViewById(R.id.rssiViewLabelMid);
        mImageViewLabelBtm = findViewById(R.id.rssiViewLabelBtm);

        mRssiImageView = findViewById(R.id.rssiImageView);
        mRssiImageView.post(new Runnable() {
            @Override
            public void run() {
                mRssiImageViewHeight = mRssiImageView.getHeight();
                mRssiImageViewWidth = mRssiImageView.getWidth();
                mRssiBitmap = Bitmap.createBitmap(mRssiImageViewWidth, mRssiImageViewHeight, Bitmap.Config.ARGB_8888);
                mRssiCanvas = new Canvas(mRssiBitmap);
                mRssiCanvas.drawColor(Color.LTGRAY);
                mBaseLinePaint = new Paint();
                mBaseLinePaint.setColor(Color.BLACK);
                mBaseLinePaint.setStrokeWidth(1);
                mRssiImageView.setImageBitmap(mRssiBitmap);

                mRssiCanvas.drawLine(0, mRssiImageViewHeight/2, mRssiImageViewWidth, mRssiImageViewHeight/2, mBaseLinePaint);
            }
        });


        File externalDir = Environment.getExternalStorageDirectory();
        mMainDir = new File(externalDir + "/DTC/" + getString(R.string.app_name));
        mMainDir.mkdirs();

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

    public void clearData(View view) {

        File[] files = mMainDir.listFiles();
        for (File file: files) {
            file.delete();
        }

        mInfoTextView.setText("Files deleted");
    }

    public void drawRssi(){

        if (!mIsLogging) return;

        mImageViewLabelTop.setText(Integer.toString(mMaxRssi));
        mImageViewLabelMid.setText(Integer.toString((mMaxRssi+mMinRssi)/2));
        mImageViewLabelBtm.setText(Integer.toString(mMinRssi));

        int[] plotData;
        double ratioX = 1.0 * mRssiImageViewWidth / MyPlotData.N_PLOT_DOTS;
        int maxData = -100;
        int minData = 100;

        mRssiCanvas.drawColor(Color.LTGRAY);
        mRssiCanvas.drawLine(0, mRssiImageViewHeight/2, mRssiImageViewWidth, mRssiImageViewHeight/2, mBaseLinePaint);

        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry: mDeviceToPosition.entrySet()) {
            if (mScanViewAdapter.mCheckBoxArray.get(entry.getValue(), false)) {

                plotData = mPlotDataMap.get(entry.getKey()).drawData();
                plotData[0] = (mMinRssi+mMaxRssi)/2;
                for (int i = 1; i < MyPlotData.N_PLOT_DOTS; i++) {
                    if (plotData[i] == 0) plotData[i] = plotData[i-1];
                    if (plotData[i] > maxData) maxData = plotData[i];
                    if (plotData[i] < minData) minData = plotData[i];
                }
                for (int i = 1; i < MyPlotData.N_PLOT_DOTS; i++) {
                    mRssiCanvas.drawLine((int)((i-1)*ratioX), (int) (1.0*mRssiImageViewHeight*(mMaxRssi-plotData[i-1])/(mMaxRssi-mMinRssi)),
                            (int)(i*ratioX), (int) (1.0*mRssiImageViewHeight*(mMaxRssi-plotData[i])/(mMaxRssi-mMinRssi)), mPaintColorList.get(colorIndex));
                }
                ++colorIndex;
                if (colorIndex > 3) colorIndex = 3;
            }
        }

        mRssiImageView.invalidate();

        mMaxRssi = maxData + 5;
        mMinRssi = minData - 5;

    }

    private void build_paintColorList() {
        // ugly codes here...
        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(ContextCompat.getColor(this, R.color.colorPaint1));
        mPaintColorList.add(paint);

        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(ContextCompat.getColor(this, R.color.colorPaint2));
        mPaintColorList.add(paint);

        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(ContextCompat.getColor(this, R.color.colorPaint3));
        mPaintColorList.add(paint);

        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(ContextCompat.getColor(this, R.color.colorPaintOthers));
        mPaintColorList.add(paint);

    }
}
