package com.bit.smartcarrier.followme;

import android.widget.Toast;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BeaconScanner2 //비콘 살 때 준 코드로 돌리는 스캐너
{
    private Kalmanfilter mKalmanFilter; // 칼만필터
    private boolean mKalmanOn;
    private ArrayList<Double> rssiBuffer = new ArrayList<Double>(); //rssi값을 누적시키는 배열
    private int mTxPower;
    private MinewBeaconManager mMinewBeaconManager;
    private Timer mRssiSendTimer;
    private boolean timerRunning;
    static final int MODE_MINEW_API = -1001;


    BeaconScanner2(MainActivity m)
    {
        init(m);
    }
    private void init(final MainActivity m)
    {
        mKalmanFilter = new Kalmanfilter(0.0);
        mMinewBeaconManager = MinewBeaconManager.getInstance(m);
        mRssiSendTimer = new Timer();
        mMinewBeaconManager.setDeviceManagerDelegateListener(new MinewBeaconManagerListener() {
            @Override
            public void onAppearBeacons(List<MinewBeacon> list) {
//                for(MinewBeacon minewBeacon : list)
//                {
//                    String deviceName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
//
//                    Toast.makeText(getApplicationContext(), deviceName + "  is appeared!", Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onDisappearBeacons(List<MinewBeacon> list) {
//                for (MinewBeacon minewBeacon : list) {
//                    String deviceName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
//                    Toast.makeText(getApplicationContext(), deviceName + "  out range", Toast.LENGTH_SHORT).show();
//                }
            }

            /*
            | index                         | 명칭     | 자료형          | 비고   |
| ----------------------------- | ------- | ----------- | ---- |
| BeaconValueIndex_UUID         | uuid    | stringValue |      |
| BeaconValueIndex_Name         | 비콘이름     | stringValue |      |
| BeaconValueIndex_Major        | major   | intValue    |      |
| BeaconValueIndex_Minor        | minor   | intValue    |      |
| BeaconValueIndex_WechatId     | 위쳇id  | intValue    | Optional |
| BeaconValueIndex_Mac          | mac주소   | stringValue | Optional |
| BeaconValueIndex_RSSI         | rssi    | intValue    |      |
| BeaconValueIndex_BatteryLevel | 베터리잔량    | intValue    |      |
| BeaconValueIndex_Temperature  | 온도      | floatValue  | Optional |
| BeaconValueIndex_Humidity     | 습도      | floatValue  | Optional |
| BeaconValueIndex_Txpower      | txPower | intValue    |      |
| BeaconValueIndex_InRange      | 비콘영역내에 있는지 여부  | boolValue   |      |
| BeaconValueIndex_Connectable  | 연결이 가능한지 여부   | boolValue   |      |
             */
            @Override
            public void onRangeBeacons(List<MinewBeacon> list) {
                for (MinewBeacon minewBeacon : list)
                {
                    //String uuid = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).getStringValue();
                    //String name = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
                    //int major = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getIntValue();
                    //int minor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getIntValue();
                    String mac = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).getStringValue();
                    int rssi = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getIntValue();
                    //int batterylevel = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_BatteryLevel).getIntValue();
                    mTxPower = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_TxPower).getIntValue();
                    //float temperature = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Temperature).getFloatValue();
                    if(!mac.equals("C2:01:EC:00:05:C6"))
                        continue;
                    double curRssi;
                    if(rssi > 0) return;

                    if (mKalmanOn)
                        curRssi = mKalmanFilter.update(rssi); //칼만 필터 사용해서 튀는 rssi값을 잡아줌
                    else
                        curRssi = rssi;
                    rssiBuffer.add(curRssi); //비콘 값을 평균내기위해 배열에 추가
                }
            }

            @Override
            public void onUpdateState(BluetoothState bluetoothState) {
                switch (bluetoothState) {
                    case BluetoothStateNotSupported:
                        Toast.makeText(m, "Not Support BLE", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothStatePowerOff:
                        Toast.makeText(m, "bluetooth off",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothStatePowerOn:
                        Toast.makeText(m, "bluetooth on",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });


    }

    void start(final MainActivity m, double intervalTime)
    {
        if(mMinewBeaconManager!=null)
            mMinewBeaconManager.startScan();
        else return;

        mRssiSendTimer = new Timer();
        mRssiSendTimer.schedule(new TimerTask()
        {
            @Override
            public void run() {
                if(rssiBuffer.isEmpty()) return;
                double rssiSum = 0;
                Iterator<Double> iter = rssiBuffer.iterator();
                while(iter.hasNext())
                {
                    rssiSum += iter.next();
                }
                rssiSum /= (double)rssiBuffer.size();
                m.updateRssi(rssiSum, MODE_MINEW_API);
                m.setTxPowerText(mTxPower);
                rssiBuffer.clear();
            }
        }, (int)(intervalTime*1000), (int)(intervalTime*1000));
        timerRunning = true;

    }

    void stop()
    {
        if(mMinewBeaconManager!=null)
            mMinewBeaconManager.stopScan();
        if (mRssiSendTimer != null && timerRunning) {
            mRssiSendTimer.cancel();
            timerRunning = false;
        }

    }

    void setKalmanOn(boolean onoff)
    {
        mKalmanOn = onoff;
    }
}
