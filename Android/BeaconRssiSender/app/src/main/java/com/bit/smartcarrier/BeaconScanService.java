package com.bit.smartcarrier;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

//비콘 찾는것을 백그라운드에서 동작
public class BeaconScanService extends Service
{
    private Kalmanfilter mKalmanFilter; // 칼만필터
    private SimpleDateFormat mSimpleDateFormat;
    private BluetoothLeScanner mBluetoothLeScanner;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //비콘 백그라운드 작업 생성시 호출
    @Override
    public void onCreate()
    {
        super.onCreate();
        mKalmanFilter = new Kalmanfilter(0.0);
        mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREAN);
        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner(); //비콘 탐지 스캐너
    }
    private ScanCallback mScanCallback = new ScanCallback()
    {
        // 비콘을 스캔할 때 마다 이 콜백 호출됨. 스캔 간격은 약 1초
        @Override
        public void onScanResult(int callbackType, final ScanResult result)
        {
            super.onScanResult(callbackType, result);
            final double filteredRssi = mKalmanFilter.update(result.getRssi()); //칼만 필터 사용해서 튀는 rssi값을 잡아줌.
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    Intent bleIntent = new Intent("MainActivity_RECEIVER");
                    bleIntent.putExtra("beacon_name", result.getDevice().getName());
                    bleIntent.putExtra("beacon_addr", result.getDevice().getAddress());
                    bleIntent.putExtra("beacon_timestamp", mSimpleDateFormat.format(new Date()));
                    bleIntent.putExtra("beacon_rssi", filteredRssi);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(bleIntent);
                }
            }).start();

        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("onBatchScanResults", results.size() + "");
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("onScanFailed()", errorCode + "");
        }

    };
    //비콘 백그라운드 작업 시작시 호출 (MainActivity.startService)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        List<ScanFilter> scanFilters;
        scanFilters = new Vector<>();
        ScanFilter.Builder scanFilter = new ScanFilter.Builder();
        scanFilter.setDeviceAddress("C2:01:EC:00:05:C6"); //특정 MAC 주소를 가진 비콘만 검색
        ScanFilter filter = scanFilter.build();
        scanFilters.add(filter);
        //filter와 settings 기능을 사용할때 아래 코드 사용
        ScanSettings.Builder scanSettings = new ScanSettings.Builder();
        scanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        mBluetoothLeScanner.startScan(scanFilters, scanSettings.build(), mScanCallback);
        // filter와 settings 기능을 사용하지 않을 때는 아래 코드 사용
        //mBluetoothLeScanner.startScan(mScanCallback);
        return super.onStartCommand(intent, flags, startId);
    }

    //비콘 백그라운드 작업 중지시 호출 (MainActivity.stopService)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeScanner != null)
            mBluetoothLeScanner.stopScan(mScanCallback); //비콘 스캔 중지
    }
}

