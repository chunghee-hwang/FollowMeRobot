package com.example.followme;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;


public class SwitchActivity extends AppCompatActivity {
    private final int GET_GALLERY_IMAGE = 200;
    private final String TAG = "smartcarrier";
    private BeaconScanner mBeaconScanner;
    private static BluetoothComm mBluetoothComm;
    private String mColorRGB="000000000";
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
        mColorRGB = intent.getStringExtra("colorRGB");
        mVibrationToggle = findViewById(R.id.vibration);
        mFollowToggle = findViewById(R.id.follow);
        mBluetoothToggle = findViewById(R.id.bluetooth);
        ToggleHandler toggleHandler = new ToggleHandler();
        mVibrationToggle.setOnCheckedChangeListener(toggleHandler);
        mFollowToggle.setOnCheckedChangeListener(toggleHandler);
        mBeaconScanner = new BeaconScanner(this);

        initBluetoothComm();

    }

    private class ToggleHandler implements ToggleButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch(buttonView.getId())
            {
                case R.id.vibration:
                    if(isChecked) {
                        buttonView.setBackgroundDrawable(getResources().
                                getDrawable(R.drawable.on));
                        mBeaconScanner.start(SwitchActivity.this);
                    }
                    else {
                        buttonView.setBackgroundDrawable(getResources().
                                getDrawable(R.drawable.off));
                        mBeaconScanner.stop();
                    }
                    break;
                case R.id.follow:
                    if(isChecked)
                    {
                        buttonView.setBackgroundDrawable(getResources().
                                getDrawable(R.drawable.on));
                        if (mBluetoothComm != null) {
                            mBluetoothComm.sendMessage(GO);
                            mBluetoothComm.sendMessage(mColorRGB);
                        }
                    }
                    else {
                        buttonView.setBackgroundDrawable(getResources().
                                getDrawable(R.drawable.off));
                        if (mBluetoothComm != null) {
                            mBluetoothComm.sendMessage(STOP);
                        }
                    }
                    break;
            }
        }
    }

    public void button1OnClick(View v) {
//        Toast.makeText(getApplicationContext(), "button2OnClick", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }

    public void button2OnClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, GET_GALLERY_IMAGE);
//        Intent intent = new Intent(getApplicationContext(), SwitchActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImageUri = data.getData();
            // imageview.setImageURI(selectedImageUri);
            Intent intent = new Intent(getApplicationContext(), ColorpickerActivity.class);
            intent.putExtra("uri", selectedImageUri);
            startActivity(intent);
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

    void iconOff()
    {
        if(mBluetoothToggle != null)
        {
            mBluetoothToggle.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.off));
            mFollowToggle.setEnabled(false);
        }
    }

    void iconOn()
    {
        if(mBluetoothToggle != null)
        {
            mBluetoothToggle.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.on));
            mFollowToggle.setEnabled(true);
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
