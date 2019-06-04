package com.bit.smartcarrier;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

//블루투스 통신을 백그라운드로 작업
public class BluetoothComm {
    ConnectedTask mConnectedTask = null; //라즈베리파이와 연결되었을 때 하는 작업
    private String mConnectedDeviceName = null; //연결된 장치 이름 
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    private MainActivity mainActivity;

    BluetoothComm(MainActivity m) {
        init(m);
    }

    private void init(MainActivity m) {
        this.mainActivity = m;
        showPairedDevicesDialog(); //MainActivity에 현재 페어링된 장치 목록 전달
    }

    void stop() {
        if (mConnectedTask != null) {
            mConnectedTask.cancel(true);
        }
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
            Toast.makeText(mainActivity.getApplicationContext(), "connecting...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // 블루투스 스캔을 멈춤
            mainActivity.mBluetoothAdapter.cancelDiscovery();

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
                Toast.makeText(mainActivity.getApplicationContext(), "Unable to connect device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //라즈베리파이와 성공적으로 연결됐다면 ConnectedTask 실행
    private void connected(BluetoothSocket socket) {
        mConnectedTask = new ConnectedTask(socket);
        Toast.makeText(mainActivity.getApplicationContext(), "connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(mainActivity.getApplicationContext(), "Device connection was lost", Toast.LENGTH_SHORT).show();
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
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mainActivity, "eraror : Server is down!", Toast.LENGTH_SHORT).show();
                    }
                });

                mainActivity.finish();
            }

        }
    }

    //MainActivity에게 현재 페어링된 장치 목록을 전달
    public void showPairedDevicesDialog() {
        //현재 페어링된 장치 목록 가져옴.
        Set<BluetoothDevice> devices = mainActivity.mBluetoothAdapter.getBondedDevices();
        String[] items;

        //Set --> 배열로 변환
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        //만약 페어링된 장치가 없다면
        if (pairedDevices.length == 0) {
            Toast.makeText(mainActivity.getApplicationContext(), "No devices have been paired.\n"
                    + "You must pair it with another device.", Toast.LENGTH_SHORT).show();
            return;
        }
        items = new String[pairedDevices.length];
        for (int i = 0; i < pairedDevices.length; i++) {
            items[i] = pairedDevices[i].getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("페어링된 장치중에 스마트캐리어가 무엇인가요?");
        //builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }

    //라즈베리파이에게 메시지를 전달하는 함수
    void sendMessage(final String msg) {
        if (mConnectedTask != null)
        {
            mConnectedTask.write(msg);
            Log.d(TAG, "send message: " + msg);
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mainActivity.mConversationText != null)
                    mainActivity.mConversationText.append("Android:" + msg + "\n");
            }
        });
    }

    private void recvMessage(String msg)
    {
        if (mConnectedDeviceName == null)
            mConnectedDeviceName = "N/A";
        mainActivity.mConversationText.append(mConnectedDeviceName + ": " + msg + "\n");
    }
}