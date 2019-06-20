package com.bit.smartcarrier.followme;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.ParseException;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private final String TAG = "smartcarrier";
    private final int REQUEST_GPS_PERM = 100;
    private final int REQUEST_BLUETOOTH_ENABLE = 200;
    private final int REQUEST_GPS_ON = 300;
    public BluetoothAdapter mBluetoothAdapter;
    //통신 기록 텍스트뷰, 블루투스 장치 정보 텍스트뷰
    public TextView mRssiText1, mRssiText2, mRssiText3, mRssiText4, mRssiText5, mDirectionText, mTxPowerText, mCommandText;
    public EditText mRssiThresholdEdit;
    private BluetoothComm mBluetoothComm;
    private BeaconScanner mBeaconScanner, mBeaconScanner2, mBeaconScanner3, mBeaconScanner4, mBeaconScanner5;
    private Compass mCompass;
    private Commander mCommander;


    //앱이 처음 실행될 때 수행됨
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();       //초기 설정
    }

    private void init() {
        //bleTextVIew  = (TextView)findViewById(R.id.bleAddrText);
        requestGPSPerm(); //사용자에게 GPS 권한 요청
        final ToggleButton kalmanToggle = findViewById(R.id.kalmanToggle);
        final ToggleButton basicScanToggle = findViewById(R.id.basicScanToggle);
        final ToggleButton compassToggle = findViewById(R.id.compassToggle);
        mRssiThresholdEdit = findViewById(R.id.rssiThresholdEdit);
        final Button rssiThresholdButton = findViewById(R.id.rssiThresholdButton);
        mDirectionText = findViewById(R.id.directionText);
        mRssiText1 = findViewById(R.id.rssiText1);
        mRssiText2 = findViewById(R.id.rssiText2);
        mRssiText3 = findViewById(R.id.rssiText3);
        mRssiText4 = findViewById(R.id.rssiText4);
        mRssiText5 = findViewById(R.id.rssiText5);
        mCommandText = findViewById(R.id.commandText);
        mCommander = new Commander(MainActivity.this);

        mCommandText.setMovementMethod(new ScrollingMovementMethod());
        kalmanToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBeaconScanner.setKalmanOn(isChecked);
                mBeaconScanner2.setKalmanOn(isChecked);
                mBeaconScanner3.setKalmanOn(isChecked);
                mBeaconScanner4.setKalmanOn(isChecked);
                mBeaconScanner5.setKalmanOn(isChecked);

            }
        });

        rssiThresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRssiThresholdEdit.getText() == null || mRssiThresholdEdit.getText().toString().equals(""))
                    return;
                try
                {
                    double rssiThreshold = Double.parseDouble(mRssiThresholdEdit.getText().toString());
                    mCommander.setRssiThreshold(rssiThreshold);
                    Toast.makeText(getApplicationContext(), "rssiThreshold:" + rssiThreshold, Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "실수 값을 입력해주세요!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        basicScanToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startBeaconScanner();
                }
                else {
                    stopBeaconScanner();
                }
            }
        });
        compassToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    startCompass();
                else
                    stopCompass();
            }
        });
        RadioButton autoRadio = findViewById(R.id.autoRadio);
        autoRadio.setChecked(true);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            LinearLayout mAutoLinear = (LinearLayout) findViewById(R.id.autoLinear);
            LinearLayout mManualLinear = (LinearLayout) findViewById(R.id.manualLinear);

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.autoRadio:
                        mManualLinear.setVisibility(View.GONE);
                        mAutoLinear.setVisibility(View.VISIBLE);

                        break;
                    case R.id.manualRadio:
                        kalmanToggle.setChecked(false);
                        basicScanToggle.setChecked(false);
                        compassToggle.setChecked(false);
                        mAutoLinear.setVisibility(View.GONE);
                        mManualLinear.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        initJoystick();

    }

    // -----------------------------------------------------------------------------------------
    // 비콘 감지 코드
    // -----------------------------------------------------------------------------------------

    //GPS 권한 요청하는 함수
    private void requestGPSPerm() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_GPS_PERM);
        } else if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            checkBluetoothAndGPSOn();
        }
    }

    //블루투스와 GPS가 켜져있는지 확인하는 함수
    private void checkBluetoothAndGPSOn() {
        boolean bluetoothOn = checkBluetooth();
        if (bluetoothOn) //블루투스가 켜져있으면
        {
            checkGPS();
        }
    }

    //블루투스가 켜져있는지 확인하는 함수
    private boolean checkBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "이 핸드폰은 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
            return false;
            //블루투스가 꺼져있으면 켜도록 요청
        } else if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BLUETOOTH_ENABLE);
            return false;
        }
        return true;
    }

    //GPS가 켜져있는지 확인하는 함수
    private boolean checkGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Toast.makeText(getApplicationContext(), "이 핸드폰은 GPS를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
            return false;

            //GPS가 꺼져있으면 켜도록 요청
        } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), "다음 설정 화면에서 GPS를 켜주세요!!", Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_GPS_ON);
            return false;
        }
        //GPS가 이미 켜져있다면
        else {
            initBeaconScanner(); //비콘 감지 스캐너 초기화
            initCompass(); //나침반 초기화
            initBluetoothComm(); //라즈베리파이와 통신 시작
            return true;
        }
    }

    //비콘 스캐너 초기화 함수
    void initBeaconScanner() {
        mBeaconScanner = new BeaconScanner(MainActivity.this, 0.5, BeaconScanner.BEACON1);
//        mBeaconScanner2 = new BeaconScanner(MainActivity.this, 0.5, BeaconScanner.BEACON2);
//        mBeaconScanner3 = new BeaconScanner(MainActivity.this, 0.5, BeaconScanner.BEACON3);
//        mBeaconScanner4 = new BeaconScanner(MainActivity.this, 0.5, BeaconScanner.BEACON4);
        mBeaconScanner5 = new BeaconScanner(MainActivity.this, 0.5, BeaconScanner.BEACON5);
    }

    void startBeaconScanner() {
        if (mBeaconScanner != null)
            mBeaconScanner.start();
//        if (mBeaconScanner2 != null)
//            mBeaconScanner2.start();
//        if (mBeaconScanner3 != null)
//            mBeaconScanner3.start();
//        if (mBeaconScanner4 != null)
//            mBeaconScanner4.start();
        if (mBeaconScanner5 != null)
            mBeaconScanner5.start();
    }

//    void startBeaconScanner2()
//    {
//        if (mBeaconScanner2 != null)
//            mBeaconScanner2.start();
//    }

    void stopBeaconScanner() {
        if (mBeaconScanner != null)
            mBeaconScanner.stop();
//        if (mBeaconScanner2 != null)
//            mBeaconScanner2.stop();
//        if (mBeaconScanner3 != null)
//            mBeaconScanner3.stop();
//        if (mBeaconScanner4 != null)
//            mBeaconScanner4.stop();
        if (mBeaconScanner5 != null)
            mBeaconScanner5.stop();

    }

//    void stopBeaconScanner2() {
//        if (mBeaconScanner2 != null)
//           mBeaconScanner2.stop();
//    }

    public void updateRssi(final double rssi, final String beaconAddress) {
        mCommander.updateRssi(rssi, beaconAddress);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (beaconAddress) {
                    case BeaconScanner.BEACON1:
                        mRssiText1.setText("RSSI1=" + String.format("%.2f", rssi) + "dBm");
                        break;
//                    case BeaconScanner.BEACON2:
//                        mRssiText2.setText("RSSI2=" + String.format("%.2f", rssi) + "dBm");
//                        break;
//                    case BeaconScanner.BEACON3:
//                        mRssiText3.setText("RSSI3=" + String.format("%.2f", rssi) + "dBm");
//                        break;
//                    case BeaconScanner.BEACON4:
//                        mRssiText4.setText("RSSI4=" + String.format("%.2f", rssi) + "dBm");
//                        break;
                    case BeaconScanner.BEACON5:
                        mRssiText5.setText("RSSI5=" + String.format("%.2f", rssi) + "dBm");
                        break;
                }

            }
        });

    }

    public void updateDirection(final int direction) {
        //mBluetoothComm.sendMessage(direction+"");
        mCommander.updateDirection(direction);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mDirectionText.setText("direction=" + direction + "");
            }
        });
    }

    public void setTxPowerText(final int txPower) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxPowerText.setText("txPower=" + txPower + "dBm");
            }
        });
    }


    //앱이 종료될때 호출되는 함수
    @Override
    protected void onDestroy() {
        stopBeaconScanner();
        //stopBeaconScanner2();
        stopBluetoothComm();
        stopCompass();
        super.onDestroy();
    }

    //GPS 권한 요청 다이얼로그에서 허용 또는 거부 버튼이 눌렸을 때 호출되는 함수
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GPS_PERM: {
                //사용자가 gps 권한 요청을 수락했을 때
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBluetoothAndGPSOn(); //gps와 블루투스 전원을 켜도록 유도

                    //gps 권한 요청을 거부당했을 때
                } else {
                    Toast.makeText(getApplicationContext(), "위치 권한이 없으면 비콘을 감지할 수 없습니다.", Toast.LENGTH_LONG).show();
                    finish(); //exit(0);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) { //블루투스 설정창에서 이 앱으로 돌아왔을 경우
            if (resultCode == RESULT_OK) { //사용자가 블루투스를 켰다면
                checkGPS();
            } else if (resultCode == RESULT_CANCELED) { //사용자가 블루투스를 켜지 않았다면
                Toast.makeText(getApplicationContext(), "블루투스를 켜야됩니다.", Toast.LENGTH_LONG).show();
                finish(); //exit(0);
            }
        } else if (requestCode == REQUEST_GPS_ON) { //GPS 설정창에서 이 앱으로 돌아왔을 경우
            if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
                checkGPS(); //gps 체크
            }
        }
    }
    // -----------------------------------------------------------------------------------------
    // 비콘 감지 코드 끝
    // -----------------------------------------------------------------------------------------

    // -----------------------------------------------------------------------------------------
    // 블루투스 통신 코드
    // -----------------------------------------------------------------------------------------

    //블루투스 통신 작업 준비 및 시작
    private void initBluetoothComm() {
        mBluetoothComm = new BluetoothComm(MainActivity.this);
    }

    private void stopBluetoothComm() {
        if (mBluetoothComm != null)
            mBluetoothComm.stop();
    }

    public BluetoothComm getBluetoothComm() {
        return mBluetoothComm;
    }

    // -----------------------------------------------------------------------------------------
    // 블루투스 통신 코드 끝
    // -----------------------------------------------------------------------------------------


    // -----------------------------------------------------------------------------------------
    // 나침반 방향 관련 코드 시작
    // -----------------------------------------------------------------------------------------

    private void initCompass() {
        mCompass = new Compass(getApplicationContext(), this);
    }

    //나침반 작동 시작
    private void startCompass() {
        if (mCompass != null)
            mCompass.start(0.5);
    }

    private void stopCompass() {
        if (mCompass != null)
            mCompass.stop();
    }
    // -----------------------------------------------------------------------------------------
    // 나침반 방향 관련 코드 끝
    // -----------------------------------------------------------------------------------------


    // -----------------------------------------------------------------------------------------
    // 조이스틱 관련 코드 시작
    // -----------------------------------------------------------------------------------------
    private void initJoystick() {
        Button upButton = findViewById(R.id.upButton);
        Button downButton = findViewById(R.id.downButton);
        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);
        upButton.setOnTouchListener(this);
        leftButton.setOnTouchListener(this);
        rightButton.setOnTouchListener(this);
        downButton.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int viewId = v.getId();
        if (viewId == R.id.upButton || viewId == R.id.downButton || viewId == R.id.leftButton || viewId == R.id.rightButton) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    switch (viewId)
                    {
                        case R.id.upButton:
                            mCommander.updateKey(Commander.UP);
                            break;
                        case R.id.downButton:
                            mCommander.updateKey(Commander.DOWN);
                            break;
                        case R.id.leftButton:
                            mCommander.updateKey(Commander.LEFT);
                            break;
                        case R.id.rightButton:
                            mCommander.updateKey(Commander.RIGHT);
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mCommander.updateKey(Commander.STOP);
                    break;
            }
        }

        return true;
    }
// -----------------------------------------------------------------------------------------
    // 조이스틱 관련 코드 끝
    // -----------------------------------------------------------------------------------------
}
