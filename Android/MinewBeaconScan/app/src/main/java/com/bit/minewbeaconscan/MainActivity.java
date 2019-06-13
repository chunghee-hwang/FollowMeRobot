package com.bit.minewbeaconscan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = findViewById(R.id.textView);
        MinewBeaconManager mMinewBeaconManager = MinewBeaconManager.getInstance(this);

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
                    String uuid = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).getStringValue();
                    String name = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
                    int major = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getIntValue();
                    int minor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getIntValue();
                    String mac = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_MAC).getStringValue();
                    int rssi = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getIntValue();
                    int batterylevel = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_BatteryLevel).getIntValue();
                    int txPower = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_TxPower).getIntValue();
                    float temperature = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Temperature).getFloatValue();
                    if(!mac.equals("C2:01:EC:00:05:C6"))
                        continue;
                    textView.setText("uuid: " + uuid+"\n" +
                            "name: " + name+"\n" +
                            "major: " + major+"\n" +
                            "minor: " + minor+"\n" +
                            "mac: " + mac+"\n" +
                            "rssi: " + rssi+"\n" +
                            "batterylevel: " + batterylevel+"\n" +
                            "txPower: " + txPower+"\n" +
                            "temperature: " + temperature+"\n");

                }
            }

            @Override
            public void onUpdateState(BluetoothState bluetoothState) {
                switch (bluetoothState) {
                    case BluetoothStateNotSupported:
                        Toast.makeText(MainActivity.this, "Not Support BLE", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case BluetoothStatePowerOff:
                        Toast.makeText(getApplicationContext(), "bluetooth off",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothStatePowerOn:
                        Toast.makeText(getApplicationContext(), "bluetooth on",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        mMinewBeaconManager.startScan();

    }
}
