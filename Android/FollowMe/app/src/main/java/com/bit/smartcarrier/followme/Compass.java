package com.bit.smartcarrier.followme;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Timer;
import java.util.TimerTask;

public class Compass implements SensorEventListener
{
    private SensorManager mSensorManager;
    private MainActivity mainActivity;
    private int mDirect;
    private Timer mDirectionTimer;
    private boolean timerRunning;

    Compass(Context context, MainActivity mainActivity)
    {
        init(context, mainActivity);
    }
    private void init(Context context, MainActivity m)
    {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mainActivity = m;
        mDirectionTimer = new Timer();
    }

    public void start(double intervalTime)
    {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
        if(mDirectionTimer==null) return;
        mDirectionTimer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                mainActivity.updateDirection(mDirect);
            }
        }, (int)(intervalTime * 1000), (int)(intervalTime * 1000));
        timerRunning = true;
    }
    public void stop()
    {
        mSensorManager.unregisterListener(this);
        if(mDirectionTimer!=null &&timerRunning)
        {
            mDirectionTimer.cancel();
            timerRunning = false;
        }
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        //get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        //북쪽  = 0로 설정
        if(degree==0){
            mDirect = 0;
        }
        //북동쪽  = 1로 설정
        else if((degree>0) && (degree < 90)){
            mDirect = 1;
        }
        //동쪽  = 2로 설정
        else if(degree==90){
            mDirect = 2;
        }
        //남동쪽  = 3로 설정
        else if((degree>90) && (degree < 180)){
            mDirect = 3;
        }
        //남쪽  = 4로 설정
        else if(degree==180){
            mDirect = 4;
        }
        //남서쪽  = 5로 설정
        else if((degree>180) && (degree < 270)){
            mDirect = 5;
        }
        //서쪽  = 6로 설정
        else if(degree==270){
            mDirect = 6;
        }
        //북서쪽  = 7로 설정
        //else if((degree>270) && (degree < 360)){
        else{
            mDirect = 7;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in use
    }
}
