package com.bit.smartcarrier;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

//비콘 찾는것을 백그라운드에서 동작
public class BeaconScanner {
    private Kalmanfilter mKalmanFilter; // 칼만필터
    private boolean mKalmanOn;
    private SimpleDateFormat mSimpleDateFormat;
    private BluetoothLeScanner mBluetoothLeScanner;
    private MainActivity mainActivity;
    private double mCurRssi;
    private String mCurTimestamp;
    private boolean isNewRssi;
    BeaconScanner(MainActivity m) {
        init(m);
    }


    //비콘 백그라운드 작업 생성시 호출
    public void init(MainActivity m) {
        this.mainActivity = m;
        mKalmanFilter = new Kalmanfilter(0.0);
        mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREAN);
        mBluetoothLeScanner = mainActivity.mBluetoothAdapter.getBluetoothLeScanner(); //비콘 탐지 스캐너

        List<ScanFilter> scanFilters;
        scanFilters = new Vector<>();
        ScanFilter.Builder scanFilter = new ScanFilter.Builder();
        scanFilter.setDeviceAddress("C2:01:EC:00:05:C6"); //특정 MAC 주소를 가진 비콘만 검색
        //scanFilter.setDeviceAddress("00:1A:7D:DA:71:13");
        ScanFilter filter = scanFilter.build();
        scanFilters.add(filter);
        //filter와 settings 기능을 사용할때 아래 코드 사용
        ScanSettings.Builder scanSettings = new ScanSettings.Builder();
        scanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        scanSettings.setReportDelay(0);
        scanSettings.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
        mBluetoothLeScanner.startScan(scanFilters, scanSettings.build(), mScanCallback);
        // filter와 settings 기능을 사용하지 않을 때는 아래 코드 사용
        //mBluetoothLeScanner.startScan(mScanCallback);
    }

    void setKalmanOn(boolean onoff) {
        mKalmanOn = onoff;
    }


    private ScanCallback mScanCallback = new ScanCallback()
    {
        // 비콘을 스캔할 때 마다 이 콜백 호출됨.

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);

            isNewRssi = true;

            if (mKalmanOn)
                mCurRssi = mKalmanFilter.update(result.getRssi()); //칼만 필터 사용해서 튀는 rssi값을 잡아줌
            else
                mCurRssi = result.getRssi();
            mCurTimestamp = mSimpleDateFormat.format(new Date());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("onBatchScanResults", results.size() + "");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("onScanFailed()", "errorCode: " + errorCode);
        }
    };

    public double getCurRssi()
    {
        return mCurRssi;
    }
    public String getCurTImestamp()
    {
        return mCurTimestamp;
    }
    public boolean isNewRssi()
    {
        return isNewRssi;
    }
    public void setNewRssi(boolean newRssi)
    {
        this.isNewRssi = newRssi;
    }



    //비콘 스캔 작업 중지
    void stop() {
        if (mBluetoothLeScanner != null)
            mBluetoothLeScanner.stopScan(mScanCallback); //비콘 스캔 중지
    }
}

