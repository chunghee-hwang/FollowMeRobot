package com.bit.smartcarrier.followme;

import android.content.Context;
import android.content.SharedPreferences;

public class Commander
{

    final static String GO ="GO";
    final static String STOP = "STOP";

    final static String SETSPEED = "SETSPEED";

    final static String SETDIRECTION = "SETDIRECTION";

    private MainActivity m;
    private String prevGoStop="";
    private int prevDirection = 0;
    private double prevSpeed = 35;
    private double rssiThreshold;
    Commander(MainActivity m){
        this.m = m;

        SharedPreferences pref = m.getSharedPreferences("followme_setting", Context.MODE_PRIVATE);
        rssiThreshold = pref.getFloat("rssiThreshold", -70);
        m.mRssiThresholdEdit.setText(rssiThreshold+"");
    }

    void updateRssi(double rssi)
    {
        if(rssi < rssiThreshold)
        {
            if(!prevGoStop.equals(GO)) {
                m.getBluetoothComm().sendMessage(GO);
                prevGoStop = GO;
            }
        }
        else if(rssi >= rssiThreshold)
        {
            if(!prevGoStop.equals(STOP)) {
                m.getBluetoothComm().sendMessage(STOP);
                prevGoStop = STOP;
                return;
            }
        }

        if(rssiThreshold -10 < rssi  && rssi <= rssiThreshold)
        {
            if(prevSpeed != 35)
            {
                m.getBluetoothComm().sendMessage(SETSPEED+" "+35);
                prevSpeed = 35;
            }
        }
        else if(rssiThreshold - 20 < rssi && rssi<= rssiThreshold-10)
        {
            if(prevSpeed != 45)
            {
                m.getBluetoothComm().sendMessage(SETSPEED+" "+45);
                prevSpeed = 45;
            }
        }
        else if(rssiThreshold - 30 < rssi && rssi <= rssiThreshold-20)
        {
            if(prevSpeed != 55)
            {
                m.getBluetoothComm().sendMessage(SETSPEED+" "+55);
                prevSpeed = 55;
            }
        }
        else if(rssiThreshold - 40 < rssi && rssi <= rssiThreshold - 30){
            if(prevSpeed != 65)
            {
                m.getBluetoothComm().sendMessage(SETSPEED+" "+65);
                prevSpeed = 65;
            }
        }
        else if(rssi <= rssiThreshold - 40)
        {
            if(prevSpeed != 70)
            {
                m.getBluetoothComm().sendMessage(SETSPEED+" "+70);
                prevSpeed = 70;
            }
        }
    }
    void updateDirection(int direction)
    {
        if(prevDirection != direction)
        {

            m.getBluetoothComm().sendMessage(SETDIRECTION+" "+direction);

            prevDirection = direction;
        }
    }

    void setRssiThreshold(double rssiThreshold)
    {
        this.rssiThreshold = rssiThreshold;
        SharedPreferences pref = m.getSharedPreferences("followme_setting",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("rssiThreshold", (float)(rssiThreshold));
        editor.apply();
    }



}
