package com.gaosiedu.mediarecord;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gaosiedu.mediarecorder.audio.AudioRecord;
import com.gaosiedu.mediarecorder.camera.CameraPreviewView;
import com.gaosiedu.mediarecorder.encoder.MediaEncode;
import com.gaosiedu.mediarecorder.render.CameraFBORender;
import com.gaosiedu.mediarecorder.shader.PROGRAM;
import com.gaosiedu.mediarecorder.util.CameraUtil;
import com.gaosiedu.mediarecorder.util.ImageTextureUtil;

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

        audioRecord = new AudioRecord();






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

        if(cameraPreviewView != null){
            cameraPreviewView.onDestory();
        }

        if(audioRecord != null){
            audioRecord.stopRecord();
        }
        if(mediaEncode != null){
            mediaEncode.stopRecord();
            mediaEncode = null;
        }


    }

    private void start() {

//        if(true){
//            cameraPreviewView.onDestory();
//            return;
//        }


        if(true){
            id = id == 0? 1 : 0;
            cameraPreviewView.switchCamera(id);
            cameraPreviewView.previewAngle(this);
            return;
        }


//        if(true){
//            cameraPreviewView.setFragmentShader(PROGRAM.REFRESH);
//            return;
//        }

        audioRecord.startRecord();

        mediaEncode = new MediaEncode(this,cameraPreviewView.getTextureId());

        mediaEncode.initEncoder(
                cameraPreviewView.getEglContext(),
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/testabc.mp4",
                MediaFormat.MIMETYPE_VIDEO_AVC,960,540,44100,2
                );

        mediaEncode.startRecord();

        audioRecord.setOnNativeCallbackPCMDataListener((buffer, size) -> {
            mediaEncode.setPCMData(buffer,size);
        });


        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.mipmap.paster_content_1);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),R.mipmap.paster_background_1);

//        Bitmap b = ImageTextureUtil.createTextImage("hahahaha",50,"#ff0000","#00000000",10);


        mediaEncode.setStickers(bitmap1,bitmap2);
        cameraPreviewView.setStickers(bitmap1,bitmap2);
        cameraPreviewView.setFragmentShader(PROGRAM.REFRESH);

    }
}
