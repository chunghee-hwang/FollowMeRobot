package com.example.followme;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

public class Compass implements SensorEventListener{
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f; //현재값
    private float prevazimuth = -999; //이전값
    private SensorManager mSensorManager;
    private Thread mThread;
    private boolean run;

    private String prevComm;
    private final String left = "LEFT";
    private final String right = "RIGHT";
    private final String straight = "STRAIGHT";

    void start(final Activity ac, final BluetoothComm bluetoothComm)
    {
        run = true;
        mSensorManager = (SensorManager)ac.getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        prevComm = "";

        final int delta = 3;
        mThread = new Thread(){

            public void run() {
                while (run) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(prevazimuth!= -999) {
                                if (prevazimuth < 60 && azimuth > 300 && !prevComm.equals(left)) {
                                    bluetoothComm.sendMessage(left);
                                    prevComm = left;
                                }
                                else if (prevazimuth > 300 && azimuth < 60 && !prevComm.equals(left)) {
                                    bluetoothComm.sendMessage(right);
                                    prevComm =right;
                                }
                                else if(Math.abs(prevazimuth - azimuth) < delta && !prevComm.equals(straight)) {
                                    bluetoothComm.sendMessage(straight);
                                    prevComm = straight;
                                }
                                else if (prevazimuth - azimuth <= -delta && !prevComm.equals(right)) {
                                    bluetoothComm.sendMessage(right);
                                    prevComm = right;
                                }
                                else if (prevazimuth - azimuth >= delta  && !prevComm.equals(left)) {
                                    bluetoothComm.sendMessage(left);
                                    prevComm = left;
                                }
                                //region
                            }
                            //endregion
                            prevazimuth = azimuth; //이전값
                        }
                    });

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        mThread.start();
    }
    void stop()
    {
        mSensorManager.unregisterListener(this);
        run = false;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.98f;
        synchronized (this)
        {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
            }

            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if(success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float)Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360; //현재값

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}