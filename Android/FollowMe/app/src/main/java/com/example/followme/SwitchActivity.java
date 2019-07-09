package com.example.followme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class SwitchActivity extends AppCompatActivity {
    private final String TAG = "smartcarrier";
    private BeaconScanner mBeaconScanner;
    private static BluetoothComm mBluetoothComm;
    private Compass mCompass;
    private String mColorRGB="000000000000000000";
    final static String GO = "GO";
    final static String STOP = "STOP";
    ToggleButton mFollowToggle;
    ToggleButton mVibrationToggle;
    ImageView mBluetoothToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);

        Intent intent = getIntent();
        mColorRGB = intent.getStringExtra("topcolorRGB") + intent.getStringExtra("botcolorRGB");

        Toast.makeText(getApplicationContext(), "상하의 색:" +mColorRGB, Toast.LENGTH_LONG).show();

        mVibrationToggle = findViewById(R.id.vibrate);
        mFollowToggle = findViewById(R.id.follow);
        mBluetoothToggle = findViewById(R.id.bluetooth);
        ToggleHandler toggleHandler = new ToggleHandler();
        mVibrationToggle.setOnCheckedChangeListener(toggleHandler);
        mFollowToggle.setOnCheckedChangeListener(toggleHandler);
        mBeaconScanner = BeaconScanner.getInstance(SwitchActivity.this);
        mCompass = new Compass();


        if(mBeaconScanner.isOn())
        {
            iconOn(R.id.vibrate);
        }
        else{
            iconOff(R.id.vibrate);
        }
        initBluetoothComm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if(intent!=null)
            mColorRGB = intent.getStringExtra("topcolorRGB") + intent.getStringExtra("botcolorRGB");
    }

    private class ToggleHandler implements ToggleButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch(buttonView.getId())
            {
                case R.id.vibrate:
                    if(isChecked) {
                        buttonView.setBackgroundDrawable(getResources().
                                getDrawable(R.drawable.on));
                        mBeaconScanner.start(SwitchActivity.this);
                    }
                    else {
                        buttonView.setBackgroundDrawable(getResources().
                                getDrawable(R.drawable.off));
                        mBeaconScanner.stop(SwitchActivity.this);
                    }
                    break;
                case R.id.follow:
                    if(isChecked)
                    {
                        buttonView.setBackgroundDrawable(getResources().
                                getDrawable(R.drawable.on));
                        if (mBluetoothComm != null) {
                            mBluetoothComm.sendMessage(GO);
//                            Toast.makeText(getApplicationContext(), "sent rgb : " + mColorRGB, Toast.LENGTH_SHORT).show();

                            mBluetoothComm.sendMessage(mColorRGB);
                           // Toast.makeText(getApplicationContext(), "sent rgb : " + mColorRGB, Toast.LENGTH_SHORT).show();
                        }
                        if(mCompass!=null && mBluetoothComm !=null)
                            mCompass.start(SwitchActivity.this, mBluetoothComm);
                    }
                    else {
                        buttonView.setBackgroundDrawable(getResources().
                                getDrawable(R.drawable.off));
                        if (mBluetoothComm != null) {
                            mBluetoothComm.sendMessage(STOP);
                        }
                        if(mCompass != null)
                            mCompass.stop();
                    }
                    break;
            }
        }
    }

    public void button1OnClick(View v) {
//        Toast.makeText(getApplicationContext(), "button2OnClick", Toast.LENGTH_SHORT).show();
        Intents intents = Intents.getInstance(getApplicationContext());
        startActivity(intents.cameraIntent);
    }

    public void button2OnClick(View v) {
        Intents intents = Intents.getInstance(SwitchActivity.this);
        startActivityForResult(intents.albumIntent, Intents.GET_GALLERY_IMAGE);
//        Intent intent = new Intent(getApplicationContext(), SwitchActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Intents.GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Intents intents = Intents.getInstance(getApplicationContext());
            // imageview.setImageURI(selectedImageUri);
//            intents.botcolorpickerIntent.putExtra("uri", intents.imageUri);
            startActivity(intents.botcolorpickerIntent);
        }
    }
    // -----------------------------------------------------------------------------------------
    // 블루투스 통신 코드
    // -----------------------------------------------------------------------------------------
    //블루투스 통신 작업 준비 및 시작
    private void initBluetoothComm() {
        mBluetoothComm = BluetoothComm.getInstance();
        mBluetoothComm.init(SwitchActivity.this);
    }

    void iconOff(int id)
    {
        if(id == R.id.bluetooth) {
            if (mBluetoothToggle != null) {
                mBluetoothToggle.setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.off));
                mFollowToggle.setEnabled(false);
            }
        }
        else if(id == R.id.vibrate) {
            if (mVibrationToggle != null) {
                mVibrationToggle.setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.off));
            }
        }
    }

    void iconOn(int id)
    {
        if(id == R.id.bluetooth) {
            if (mBluetoothToggle != null) {
                mBluetoothToggle.setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.on));
                mFollowToggle.setEnabled(true);
            }
        }
        else if(id == R.id.vibrate) {
            if (mVibrationToggle != null) {
                mVibrationToggle.setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.on));
            }
        }
    }

    public static void stopBluetoothComm() {
        if (mBluetoothComm != null) mBluetoothComm.stop();
    }

    // -----------------------------------------------------------------------------------------
    // 블루투스 통신 코드 끝
    // -----------------------------------------------------------------------------------------




    //앱이 종료될때 호출되는 함수
    @Override
    protected void onDestroy()
    {
//        stopBluetoothComm();
//        if(mBeaconScanner!=null)
//            mBeaconScanner.stop();
        super.onDestroy();
    }

}
