package com.bit.smartcarrier;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

//비콘 찾는것을 백그라운드에서 동작
public class BeaconScanner {
    private Kalmanfilter mKalmanFilter; // 칼만필터
    private boolean mKalmanOn;
    private SimpleDateFormat mSimpleDateFormat;
    private BluetoothLeScanner mBluetoothLeScanner;
    private MainActivity mainActivity;
    private double mCurRssi;
    private RssiScanner mRssiscanner;
    private Timer mRssiSendTimer;
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
        ScanFilter filter = scanFilter.build();
        scanFilters.add(filter);
        //filter와 settings 기능을 사용할때 아래 코드 사용
        ScanSettings.Builder scanSettings = new ScanSettings.Builder();
        scanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        //scanSettings.setReportDelay(0);
        //scanSettings.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
        mRssiscanner = new RssiScanner(0.5);
        mBluetoothLeScanner.startScan(scanFilters, scanSettings.build(), mRssiscanner);
        // filter와 settings 기능을 사용하지 않을 때는 아래 코드 사용
        //mBluetoothLeScanner.startScan(mScanCallback);
    }

    void setKalmanOn(boolean onoff)
    {
        mKalmanOn = onoff;
    }


    //비콘이 폰으로 rssi값을 쐈을 때 값을 누적했다가 평균값을 취해서
    //0.5초 간격으로
    //라즈베리파이로 보냄
    private class RssiScanner extends ScanCallback
    //private ScanCallback mScanCallback = new ScanCallback()
    {
        private ArrayList<Double> rssiBuffer = new ArrayList<Double>(); //rssi값을 누적시키는 배열
        private double mIntervalTime; //라즈베리로 rssi 평균값을 보내는 주기

        RssiScanner(double intervalTime)
        {
            this.mIntervalTime = intervalTime;
            mRssiSendTimer = new Timer();

            //타이머를 써서 0.5초 간격으로 rssi 평균값을 라즈베리파이로 보냄
            TimerTask timerTask = new TimerTask()
            {
                @Override
                public void run() {

                    if(rssiBuffer.isEmpty()) return;
                    double rssiSum = 0;
                    for(double rssi : rssiBuffer){
                        rssiSum += rssi;
                    }
                    rssiSum /= (double)rssiBuffer.size();
                    mainActivity.sendMessage(rssiSum);
                    rssiBuffer.clear();
                }
            };
            mRssiSendTimer.schedule(timerTask, (int)(mIntervalTime * 1000), (int)(mIntervalTime * 1000));
        }

        //비콘에서 보낸 rssi를 받는 함수
        @Override
        public void onScanResult(int callbackType, final ScanResult result)
        {
            super.onScanResult(callbackType, result);
            //isNewRssi = true;
            int rssi = result.getRssi();
            if(rssi > 0) return;

            if (mKalmanOn)
                mCurRssi = mKalmanFilter.update(rssi); //칼만 필터 사용해서 튀는 rssi값을 잡아줌
            else
                mCurRssi = rssi;
            rssiBuffer.add(mCurRssi); //비콘 값을 평균내기위해 배열에 추가

            //화면에 rssi 배열 출력
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.mConversationText.append(rssiBuffer.toString()+"\n");
                }
            });
            //mCurTimestamp = mSimpleDateFormat.format(new Date());
            //mainActivity.sendMessage(mCurRssi);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);
            Log.d("onBatchScanResults", results.size() + "");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("onScanFailed()", "errorCode: " + errorCode);
        }
    }

//    public double getCurRssi()
//    {
//        return mCurRssi;
//    }
//    public String getCurTImestamp()
//    {
//        return mCurTimestamp;
//    }
//    public boolean isNewRssi()
//    {
//        return isNewRssi;
//    }
//    public void setNewRssi(boolean newRssi)
//    {
//        this.isNewRssi = newRssi;
//    }



    //비콘 스캔 작업 중지
    void stop() {
        if (mBluetoothLeScanner != null)
            mBluetoothLeScanner.stopScan(mRssiscanner); //비콘 스캔 중지
        if(mRssiSendTimer!=null)
            mRssiSendTimer.cancel();
    }
}

