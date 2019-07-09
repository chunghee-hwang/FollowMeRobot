package com.example.followme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //카메라 버튼이 눌렸을 때
    public void button1OnClick(View v) {
        Intents intents = Intents.getInstance(getApplicationContext());
        startActivity(intents.cameraIntent);
    }

    //
    public void button2OnClick(View v) {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        Intents intents = Intents.getInstance(MainActivity.this);
        startActivityForResult(intents.albumIntent, Intents.GET_GALLERY_IMAGE);
        Toast.makeText(getApplicationContext(), "상의 사진을 선택해주세요!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Intents.GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Intents intents = Intents.getInstance(getApplicationContext());
            intents.top_imageUri = data.getData();

//            Intent intent = new Intent(getApplicationContext(), Bot_colorpickerActivity.class);
//            intent.putExtra("uri", selectedImageUri);
//            startActivity(intent);


//            intents.botcolorpickerIntent.putExtra("uri", intents.imageUri);
            startActivity(intents.topcolorpickerIntent);
        }
    }

    @Override
    protected void onDestroy() {
        BluetoothComm.getInstance().stop();
        BeaconScanner.getInstance(MainActivity.this).stop(MainActivity.this);
        super.onDestroy();
    }
}
