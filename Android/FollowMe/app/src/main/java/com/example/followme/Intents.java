package com.example.followme;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

//인텐트(액티비티 정보)들 모아놓은 클래스
public class Intents {
    static final int GET_GALLERY_IMAGE = 200;
    Intent cameraIntent;
    Intent albumIntent;
    Intent switchIntent;
    Intent botcolorpickerIntent;
    Intent topcolorpickerIntent;
    Uri bot_imageUri, top_imageUri;
    private static Intents mIntents;
    private Intents(Context context){
        cameraIntent = new Intent(context, CameraActivity.class);
        albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        switchIntent = new Intent(context, SwitchActivity.class);
        botcolorpickerIntent = new Intent(context, Bot_colorpickerActivity.class);
        topcolorpickerIntent = new Intent(context, Top_colorpickerActivity.class);

        //액티비티가 쌓이지 않게하여 메모리가 부족하지 않게함.
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        albumIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        switchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        botcolorpickerIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        topcolorpickerIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    public static Intents getInstance(Context context)
    {
        if(mIntents == null)
        {
            mIntents = new Intents(context);
        }
        return mIntents;
    }

}
