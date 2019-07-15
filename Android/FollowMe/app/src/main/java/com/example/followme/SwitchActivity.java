package com.example.followme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    String[] REQUIRED_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSIONS_REQUEST_CODE = 1011;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);

        Intent intent = getIntent();
        mColorRGB = intent.getStringExtra("topcolorRGB") + intent.getStringExtra("botcolorRGB");

        //Toast.makeText(getApplicationContext(), "상하의 색:" +mColorRGB, Toast.LENGTH_LONG).show();

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
            int id = buttonView.getId();
            switch(id)
            {
                case R.id.vibrate:
                    if(isChecked) {
                        iconOn(id);
                        mBeaconScanner.start(SwitchActivity.this);
                    }
                    else {
                        iconOff(id);
                        mBeaconScanner.stop(SwitchActivity.this);
                    }
                    break;
                case R.id.follow:
                    if(isChecked)
                    {
                        iconOn(id);
                        if (mBluetoothComm != null) {
                            mBluetoothComm.sendMessage(GO);

                            mBluetoothComm.sendMessage(mColorRGB);
                        }
                        if(mCompass!=null && mBluetoothComm !=null)
                            mCompass.start(SwitchActivity.this, mBluetoothComm);
                    }
                    else {
                        iconOff(id);
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
        Intents intents = Intents.getInstance(getApplicationContext());
        startActivity(intents.cameraIntent);
    }

    public void button2OnClick(View v) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

            Snackbar.make(findViewById(R.id.layout_main), "이 앱을 실행하려면 외부 저장소 접근 권한이 필요합니다.",
                    Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    ActivityCompat.requestPermissions(SwitchActivity.this, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE);
                }
            }).show();


        } else {
            // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
            // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Intents.GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Intents intents = Intents.getInstance(getApplicationContext());
            intents.top_imageUri = data.getData();
            Top_colorpickerActivity.onCreateCalled = false;
            startActivity(intents.topcolorpickerIntent);

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
            }
        }
        else if(id == R.id.follow)
        {
            if (mFollowToggle != null) {
                mFollowToggle.setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.off));
            }
        }
        else if(id == R.id.vibrate) {
            if (mVibrationToggle != null) {
                mVibrationToggle.setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.off));
                mVibrationToggle.setChecked(false);
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
        else if(id == R.id.follow)
        {
            if (mFollowToggle != null) {
                mFollowToggle.setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.on));
            }
        }
        else if(id == R.id.vibrate) {
            if (mVibrationToggle != null) {
                mVibrationToggle.setBackgroundDrawable(getResources().
                        getDrawable(R.drawable.on));
                mVibrationToggle.setChecked(true);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if (requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            boolean check_result = true;

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {

                Intents intents = Intents.getInstance(SwitchActivity.this);
                startActivityForResult(intents.albumIntent, Intents.GET_GALLERY_IMAGE);
                Toast.makeText(getApplicationContext(), "상의 사진을 선택해주세요!", Toast.LENGTH_SHORT).show();
            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Snackbar.make(findViewById(R.id.layout_main), "퍼미션이 거부되었습니다. 저장소에 접근하기 위해 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                        }
                    }).show();

                } else {

                    Snackbar.make(findViewById(R.id.layout_main), "설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                        }
                    }).show();
                }
            }

        }


    }
}
