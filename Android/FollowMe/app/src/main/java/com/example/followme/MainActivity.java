package com.example.followme;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private final int GET_GALLERY_IMAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

}
