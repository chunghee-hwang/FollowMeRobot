package com.bit.smartcarrier.followme;

import android.content.Context;
import android.content.SharedPreferences;

public class Commander {
    final static String GO = "GO";
    final static String STOP = "STOP";

    final static String SETSPEED = "SETSPEED";

    final static String SETDIRECTION = "SETDIRECTION";

    final static String UP = "UP";
    final static String DOWN = "DOWN";
    final static String LEFT = "LEFT";
    final static String RIGHT = "RIGHT";

    private MainActivity m;
    private String prevGoStop = "";
    private int prevDirection = 0;
    private double prevSpeed = 35;
    private double rssiThreshold;

    Commander(MainActivity m) {
        this.m = m;

        SharedPreferences pref = m.getSharedPreferences("followme_setting", Context.MODE_PRIVATE);
        rssiThreshold = pref.getFloat("rssiThreshold", -70);
        m.mRssiThresholdEdit.setText(rssiThreshold + "");
    }

    void updateRssi(double rssi) {
        final int delta = 5;
        if (rssiThreshold <= rssi) {
            //if (!prevGoStop.equals(STOP)) {
                m.getBluetoothComm().sendMessage(STOP);
                prevGoStop = STOP;
                return;
           // }
        } else {
            //if (!prevGoStop.equals(GO)) {
                m.getBluetoothComm().sendMessage(GO);
                prevGoStop = GO;
           // }
        }

        if (rssiThreshold - delta * 2 <= rssi && rssi < rssiThreshold - delta) {
            if (prevSpeed != 30) {
                m.getBluetoothComm().sendMessage(SETSPEED + " " + 35);
                prevSpeed = 30;
            }
        } else if (rssiThreshold - delta * 3 <= rssi && rssi < rssiThreshold - delta * 2) {
            if (prevSpeed != 40) {
                m.getBluetoothComm().sendMessage(SETSPEED + " " + 45);
                prevSpeed = 40;
            }
        } else if (rssiThreshold - delta * 4 <= rssi && rssi < rssiThreshold - delta * 3) {
            if (prevSpeed != 50) {
                m.getBluetoothComm().sendMessage(SETSPEED + " " + 55);
                prevSpeed = 50;
            }
        } else if (rssiThreshold - delta * 5 <= rssi && rssi < rssiThreshold - delta * 4) {
            if (prevSpeed != 65) {
                m.getBluetoothComm().sendMessage(SETSPEED + " " + 65);
                prevSpeed = 65;
            }
        } else if (rssi <= rssiThreshold - delta * 5) {
            if (prevSpeed != 60) {
                m.getBluetoothComm().sendMessage(SETSPEED + " " + 70);
                prevSpeed = 60;
            }
        }
    }

    void updateDirection(int direction) {
        if (prevDirection != direction) {

            m.getBluetoothComm().sendMessage(SETDIRECTION + " " + direction);

            prevDirection = direction;
        }
    }

    void updateKey(String keyname)
    {
       m.getBluetoothComm().sendMessage(keyname);
    }


    void setRssiThreshold(double rssiThreshold) {
        this.rssiThreshold = rssiThreshold;
        SharedPreferences pref = m.getSharedPreferences("followme_setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("rssiThreshold", (float) (rssiThreshold));
        editor.apply();
    }


}
