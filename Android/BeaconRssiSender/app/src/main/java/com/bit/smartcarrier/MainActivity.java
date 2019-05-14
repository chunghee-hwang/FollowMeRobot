package com.bit.smartcarrier;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "smartcarrier";
    private final int REQUEST_GPS_PERM = 100;
    private final int REQUEST_BLUETOOTH_ENABLE = 200;
    private final int REQUEST_GPS_ON = 300;
    private BroadcastReceiver mReceiver;
    static BluetoothAdapter mBluetoothAdapter;
    //통신 기록 텍스트뷰, 블루투스 장치 정보 텍스트뷰
    private TextView conversationText, bleTextVIew;
    //앱이 처음 실행될 때 수행됨
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();       //초기 설정
    }
    private void init()
    {
        conversationText  = (TextView)findViewById(R.id.conversationText);
        bleTextVIew  = (TextView)findViewById(R.id.bleAddrText);
        requestGPSPerm(); //사용자에게 GPS 권한 요청
        ToggleButton kalmanToggle = findViewById(R.id.kalmanToggle);
        final EditText rssiThresholdEdit = findViewById(R.id.rssiThresholdEdit);
        final Button rssiThresholdButton = findViewById(R.id.rssiThresholdButton);
        Button scrollDownButton = findViewById(R.id.scrollDownButton);
        final ScrollView conversationScroll = findViewById(R.id.conversationScroll);
        scrollDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationScroll.fullScroll(View.FOCUS_DOWN);
            }
        });

        kalmanToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent("BeaconScanService_RECEIVER");
                if(isChecked)
                {
                    intent.putExtra("kalmanOnOff", true);
                }
                else
                {
                    intent.putExtra("kalmanOnOff", false);
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        });

        rssiThresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rssiThresholdEdit.getText()== null || rssiThresholdEdit.getText().toString().equals(""))
                    return;
                double rssiThreshold = Double.parseDouble(rssiThresholdEdit.getText().toString());
                sendMsgToBluetoothCommService("th" + rssiThreshold);
            }
        });
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
        else
        {
            initBeaconScanService(); //비콘 감지 시작
            initBluetoothService();
            return true;
        }

    }

    //비콘 스캐너 초기화 함수
    void initBeaconScanService()
    {
        Thread t1 = new Thread(){
            public void run(){
                while(true) {
                    final String msg = "time: " + BeaconScanService.curTimestamp + "rssi :" + String.format("%.2f", BeaconScanService.curRSSI) + "\n";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            conversationText.append("Me: " + msg + "\n");
                        }
                    });

                    sendMsgToBluetoothCommService(String.format("%.2f", BeaconScanService.curRSSI));
                    try {
                        Thread.sleep(500);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
        t1.setDaemon(true);
        t1.start();


        startService(new Intent(getApplicationContext(), BeaconScanService.class));
    }

    //앱이 종료될때 호출되는 함수
    @Override
    protected void onDestroy() {
        //비콘 감지 백그라운드 동작과
        //블루투스 통신 백그라운드 동작을 멈춤
        stopService(new Intent(getApplicationContext(), BeaconScanService.class));
        stopService(new Intent(getApplicationContext(), BluetoothCommService.class));
        if(mReceiver != null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    //GPS 권한 요청 다이얼로그에서 허용 또는 거부 버튼이 눌렸을 때 호출되는 함수
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
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
    
    //블루투스 통신 백그라운드 작업 준비 및 시작
    private void initBluetoothService()
    {
        Toast.makeText(getApplicationContext()," init bluetoothservice",Toast.LENGTH_SHORT).show();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("MainActivity_RECEIVER2");
        intentfilter.addAction("MainActivity_RECEIVER3");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                //BluetoothService 클래스에서 현재 페어링된 장치들을 받았다면
                if(intent.getAction() == null) return;
                if(intent.getAction().equals("MainActivity_RECEIVER2"))
                {
                    String []pairedDeviceNames = intent.getStringArrayExtra("pairedDevices");
                    
                    //페어링된 장치 목록을 창으로 띄움.
                    if(pairedDeviceNames != null)
                        showPairedDevicesListDialog(pairedDeviceNames);
                    else
                        Toast.makeText(getApplicationContext(), "Cannot receive the paired device!", Toast.LENGTH_SHORT).show();
                }
                else if(intent.getAction().equals("MainActivity_RECEIVER3"))
                {
                    String connectedDeviceName = intent.getStringExtra("connectedDeviceName");
                    String recvMessage = intent.getStringExtra("recvMessage");
                    conversationText.append(connectedDeviceName+":" + recvMessage+"\n");
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentfilter);
        
        //블루투스 통신 백그라운드 작업 시작
        startService(new Intent(getApplicationContext(), BluetoothCommService.class));
    }

    //string 값을 BluetoothService 클래스에게 전달
    private void sendMsgToBluetoothCommService(String msg)
    {
        Intent intent = new Intent("BluetoothCommService_RECEIVER2");
        intent.putExtra("msg", msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    //페어링된 장치 목록을 창으로 띄움.
    private void showPairedDevicesListDialog(String []pairedDeviceNames)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("페어링된 장치중에 스마트캐리어가 무엇인가요?");
        builder.setCancelable(false);
        builder.setItems(pairedDeviceNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent("BluetoothCommService_RECEIVER");
                intent.putExtra("selectedPairedDeviceIndex", which);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        });
        builder.create().show();

    }
    // -----------------------------------------------------------------------------------------
    // 블루투스 통신 코드 끝
    // -----------------------------------------------------------------------------------------

}
