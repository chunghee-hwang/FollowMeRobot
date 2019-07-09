package com.example.followme;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class BluetoothComm {
    ConnectedTask mConnectedTask = null; //라즈베리파이와 연결되었을 때 하는 작업
    private String mConnectedDeviceName = null; //연결된 장치 이름
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    private Activity mAc;
    private BluetoothAdapter mBluetoothAdapter;
    public static boolean isConnected;
    private static BluetoothComm mBluetoothComm;

    private BluetoothComm() {
    }

    public static BluetoothComm getInstance() {
        if (mBluetoothComm == null)
            mBluetoothComm = new BluetoothComm();
        return mBluetoothComm;
    }

    void init(final Activity ac) {
        this.mAc = ac;
        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(ac,
                        // onRequirementsFulfilled
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                try {
                                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    if (!isConnected) {
                                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("8C:88:2B:00:09:A0");
                                        ConnectTask task = new ConnectTask(device);
                                        task.execute();
                                    } else {
                                        ((SwitchActivity) ac).iconOn(R.id.bluetooth);
                                    }
                                    //Toast.makeText(ac, "requirements fulfilled", Toast.LENGTH_SHORT).show();
                                } catch (final Exception e) {
                                    Toast.makeText(ac, "에러 : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                return null;
                            }
                        },
                        // onRequirementsMissing
                        new Function1<List<? extends Requirement>, Unit>() {
                            @Override
                            public Unit invoke(List<? extends Requirement> requirements) {
                                Toast.makeText(ac, "요구사항이 부족합니다: " + requirements, Toast.LENGTH_SHORT).show();
                                ac.finish();
                                return null;
                            }
                        },
                        // onError
                        new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Toast.makeText(ac, "요구사항 에러: " + throwable, Toast.LENGTH_SHORT).show();
                                ac.finish();
                                return null;
                            }
                        });
    }

    void stop() {
        if (mConnectedTask != null) {
            mConnectedTask.cancel(true);
        }


        if (mAc != null) {
            ((SwitchActivity) mAc).iconOff(R.id.bluetooth);
            if(isConnected)
                Toast.makeText(mAc, "연결을 해제합니다.", Toast.LENGTH_SHORT).show();
        }
        isConnected = false;
    }

    //라즈베리파이와 통신하기위한 클래스
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private BluetoothSocket mBluetoothSocket = null;//블루투스 소켓
        private BluetoothDevice mBluetoothDevice = null;//연결된 블루투스 장치
        private final int bluetooth_port = 1; //라즈베리파이의 파이썬 코드의 port번호와 동일해야함.

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;

            mConnectedDeviceName = bluetoothDevice.getName();
            //SPP
            //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            try {
                //파이썬 코드와 호환되기 위해 uuid 대신 port 번호 사용
                Method m = mBluetoothDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                mBluetoothSocket = (BluetoothSocket) m.invoke(mBluetoothDevice, bluetooth_port); //소켓 생성
                Log.d(TAG, "create socket for " + mConnectedDeviceName);
            } catch (Exception e) {
                Log.e(TAG, "socket create failed " + e.getMessage());
            }
            Toast.makeText(mAc, "연결 중...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // 블루투스 스캔을 멈춤
            mBluetoothAdapter.cancelDiscovery();

            // 라즈베리파이에 연결을 시도함.
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                // 에러가 나면 소켓 닫음.
                try {
                    mBluetoothSocket.close();

                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            if (isSucess) {
                connected(mBluetoothSocket);
            } else {

                isConnectionError = true;
                Toast.makeText(mAc, "장치에 연결하지 못했습니다.", Toast.LENGTH_SHORT).show();
                showPairedDevicesDialog();
                isConnected = false;
            }
        }
    }

    //라즈베리파이와 성공적으로 연결됐다면 ConnectedTask 실행
    private void connected(BluetoothSocket socket) {
        mConnectedTask = new ConnectedTask(socket);
        if(mAc != null) {
            Toast.makeText(mAc, mConnectedDeviceName + "에 연결되었습니다.", Toast.LENGTH_SHORT).show();

            ((SwitchActivity) mAc).iconOn(R.id.bluetooth);
        }
        isConnected = true;

    }

    //라즈베리파이와 String 값을 주고받는 작업을 하는 클래스
    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private BluetoothSocket mBluetoothSocket;

        ConnectedTask(final BluetoothSocket socket) {
            new Thread() {
                public void run() {
                    mBluetoothSocket = socket;
                    try {
                        //String 주고 받을 스트림 생성
                        mInputStream = mBluetoothSocket.getInputStream();
                        mOutputStream = mBluetoothSocket.getOutputStream();
                        ConnectedTask.this.execute();
                    } catch (IOException e) {
                        Log.e(TAG, "socket not created", e);
                        isConnected = false;
                        if (mAc != null) {
                            ((SwitchActivity) mAc).iconOff(R.id.bluetooth);
                        }
                    }
                }
            }.start();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            byte[] readBuffer = new byte[100];
            int readBufferPosition = 0;
            while (true) {
                if (isCancelled()) return false;
                try {
                    int bytesAvailable = mInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mInputStream.read(packetBytes);

                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);

                                //라즈베리파이가 보낸 메시지를 받음.
                                //byte[] --> String으로 변환
                                String recvMessage = new String(encodedBytes, "UTF-8");
                                readBufferPosition = 0;
                                publishProgress(recvMessage);
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    return false;
                }
            }

        }

        //라즈베리파이로부터 메시지를 받았다면 MainActivity에게 받은 메시지값 전달
        @Override
        protected void onProgressUpdate(String... recvMessage) {
            recvMessage(recvMessage[0]);
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);

            if (!isSucess) {
                closeSocket();
                isConnectionError = true;
                Toast.makeText(mAc, "장치 연결이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
                if (mAc != null) {
                    mAc.finish();
                    ((SwitchActivity) mAc).iconOff(R.id.bluetooth);
                }
                isConnected = false;
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);

            closeSocket();
        }

        //블루투스 소켓 닫기
        void closeSocket() {
            try {

                mBluetoothSocket.close();

                Log.d(TAG, "close socket()");

            } catch (IOException e2) {

                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }

        //라즈베리파이에게 스트림을 통해 String 값을 전달
        //write함수가 그 역할을 함
        void write(String msg) {

            msg += "\n";
            try {

                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();

            } catch (IOException e) {
                Log.e(TAG, "Exception during send", e);
                isConnected = false;
                if (mAc != null) {
                    mAc.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mAc, "에러 : 서버가 다운되었습니다!", Toast.LENGTH_SHORT).show();
                            ((SwitchActivity) mAc).iconOff(R.id.bluetooth);
                        }
                    });
                }


                mAc.finish();
            }

        }
    }

    //MainActivity에게 현재 페어링된 장치 목록을 전달
    public void showPairedDevicesDialog() {
        //현재 페어링된 장치 목록 가져옴.
        if (mBluetoothAdapter == null) {
            Toast.makeText(mAc, "bluetooth adapter is null!", Toast.LENGTH_LONG).show();
            return;
        }
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        String[] items;

        //Set --> 배열로 변환
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        //만약 페어링된 장치가 없다면
        if (pairedDevices.length == 0) {
            Toast.makeText(mAc, "페어링된 장치가 없습니다. 페어링 먼저 진행해주세요!", Toast.LENGTH_SHORT).show();
            mAc.finish();
            return;
        }
        items = new String[pairedDevices.length];
        for (int i = 0; i < pairedDevices.length; i++) {
            items[i] = pairedDevices[i].getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mAc);
        builder.setTitle("페어링된 장치중에 FollowMe 로봇이 무엇인가요?");
        builder.setCancelable(false);
        builder.setNegativeButton("연결 취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mAc!=null) {
                    Toast.makeText(mAc, "연결을 취소합니다.", Toast.LENGTH_SHORT).show();
                    ((SwitchActivity) mAc).iconOff(R.id.bluetooth);

                    mAc.finish();
                }
                isConnected = false;

            }
        });
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        try {
            builder.create().show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //라즈베리파이에게 메시지를 전달하는 함수
    void sendMessage(final String msg) {
        if (mConnectedTask != null) {
            mConnectedTask.write(msg);
            Log.d(TAG, "send message: " + msg);
        }
//        mAc.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(mAc, "Android: " + msg, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void recvMessage(String msg) {

    }
}
