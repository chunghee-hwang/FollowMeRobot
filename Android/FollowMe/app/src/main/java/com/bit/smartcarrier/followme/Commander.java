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
    private String prevCommand = "";
    private int prevDirection = 0;
    private double prevSpeed = 35;
    private double rssiThreshold;

    private double mRssis[];
    Commander(MainActivity m) {
        this.m = m;
        SharedPreferences pref = m.getSharedPreferences("followme_setting", Context.MODE_PRIVATE);
        rssiThreshold = pref.getFloat("rssiThreshold", -70);
        m.mRssiThresholdEdit.setText(rssiThreshold + "");
        mRssis = new double[2];
        mRssis[0] = 0;
        mRssis[1] = 1;
    }

    void updateRssi(double rssi, String beaconAddress)
    {
        final int delta = 5;

        switch (beaconAddress)
        {
            case BeaconScanner.BEACON1:
                mRssis[0] = rssi;
                break;
            case BeaconScanner.BEACON2:
                mRssis[1] = rssi;
                break;
        }

//        if (rssiThreshold <= mRssis[0] && rssiThreshold <= mRssis[1]) {
//            if (!prevCommand.equals(STOP)) {
//                m.getBluetoothComm().sendMessage(STOP);
//                prevCommand = STOP;
//                return;
//            }
//        } else {
//            //if (!prevCommand.equals(GO)) {
//                m.getBluetoothComm().sendMessage(GO);
//                prevCommand = GO;
//           // }
//        }

        if(mRssis[0] - mRssis[1] > 20) {
            if (!prevCommand.equals(LEFT)){
                m.getBluetoothComm().sendMessage(LEFT);
                prevCommand = LEFT;
            }
        }
        else if(mRssis[1] - mRssis[0] > 20) {
            if (!prevCommand.equals(RIGHT)) {
                m.getBluetoothComm().sendMessage(RIGHT);
                prevCommand = RIGHT;
            }
        }
        else {
            if (!prevCommand.equals(STOP)) {
                m.getBluetoothComm().sendMessage(STOP);
                prevCommand = STOP;
            }
        }

//        double rssiAvg = (mRssis[0] + mRssis[1]) / 2.0f;
//
//        if (rssiThreshold - delta * 2 <= rssiAvg && rssiAvg < rssiThreshold - delta) {
//            if (prevSpeed != 30) {
//                m.getBluetoothComm().sendMessage(SETSPEED + " " + 35);
//                prevSpeed = 30;
//            }
//        } else if (rssiThreshold - delta * 3 <= rssiAvg && rssiAvg < rssiThreshold - delta * 2) {
//            if (prevSpeed != 40) {
//                m.getBluetoothComm().sendMessage(SETSPEED + " " + 45);
//                prevSpeed = 40;
//            }
//        } else if (rssiThreshold - delta * 4 <= rssiAvg && rssiAvg < rssiThreshold - delta * 3) {
//            if (prevSpeed != 50) {
//                m.getBluetoothComm().sendMessage(SETSPEED + " " + 55);
//                prevSpeed = 50;
//            }
//        } else if (rssiThreshold - delta * 5 <= rssiAvg && rssiAvg < rssiThreshold - delta * 4) {
//            if (prevSpeed != 65) {
//                m.getBluetoothComm().sendMessage(SETSPEED + " " + 65);
//                prevSpeed = 65;
//            }
//        } else if (rssiAvg <= rssiThreshold - delta * 5) {
//            if (prevSpeed != 60) {
//                m.getBluetoothComm().sendMessage(SETSPEED + " " + 70);
//                prevSpeed = 60;
//            }
//        }


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
