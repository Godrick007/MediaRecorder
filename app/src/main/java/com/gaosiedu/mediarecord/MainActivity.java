package com.gaosiedu.mediarecord;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaFormat;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.gaosiedu.mediarecorder.audio.AudioRecord;
import com.gaosiedu.mediarecorder.camera.CameraPreviewView;
import com.gaosiedu.mediarecorder.encoder.MediaEncode;
import com.gaosiedu.mediarecorder.shader.PROGRAM;
import com.gaosiedu.mediarecorder.util.CameraUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    CameraPreviewView cameraPreviewView;

    Button btnStart, btnStop;

    AudioRecord audioRecord;

    private MediaEncode mediaEncode;

    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };

        requestPermissions(permissions, 100);

        Camera.Size size = CameraUtil.getCameraSize(this);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        RelativeLayout rlContent = findViewById(R.id.fl_content);



        cameraPreviewView = new CameraPreviewView(this,size.width,size.height);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size.width,size.height);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        cameraPreviewView.setLayoutParams(params);
        rlContent.addView(cameraPreviewView);


        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);


        cameraPreviewView.setOnTakePhotoListener(this::saveBitmap);





    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_start:
                start();
                break;
            case R.id.btn_stop:
                stop();
                break;


        }

    }

    private void stop() {

        cameraPreviewView.takePhoto();

        if(cameraPreviewView != null){
//            cameraPreviewView.release();
        }

        if(audioRecord != null){
            audioRecord.stopRecord();
            audioRecord.release();
            audioRecord = null;
        }
        if(mediaEncode != null){
            mediaEncode.stopRecord();
            mediaEncode = null;
        }


    }

    private void start() {

//        if(true){
//            cameraPreviewView.release();
//            return;
//        }


//        if(true){
//            id = id == 0? 1 : 0;
//            cameraPreviewView.switchCamera(id);
//            cameraPreviewView.previewAngle(this);
//            return;
//        }


//        if(true){
//            cameraPreviewView.setFragmentShader(PROGRAM.REFRESH);
//            return;
//        }


        audioRecord = new AudioRecord();

        audioRecord.startRecord();

        mediaEncode = new MediaEncode(this,cameraPreviewView.getTextureId());

        mediaEncode.initEncoder(
                cameraPreviewView.getEglContext(),
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/testabc.mp4",
                MediaFormat.MIMETYPE_VIDEO_AVC,960,540,44100,2
                );

        mediaEncode.startRecord();

        audioRecord.setOnNativeCallbackPCMDataListener((buffer, size) -> {
            Log.e("audio recorder","size is " + size);
            mediaEncode.setPCMData(buffer,size);
        });


        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.img_capture_sticker_donkey_1);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),R.drawable.img_capture_sticker_donkey_2);

//        Bitmap b = ImageTextureUtil.createTextImage("hahahaha",50,"#ff0000","#00000000",10);


        mediaEncode.setStickers(bitmap1,bitmap2);
        cameraPreviewView.setStickers(bitmap1,bitmap2);
        cameraPreviewView.setFragmentShader(PROGRAM.REFRESH);

    }

    public void saveBitmap(Bitmap b){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/haha";

        long dataTake = System.currentTimeMillis();
        final String jpegName=path+ dataTake +".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
