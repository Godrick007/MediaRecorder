package com.gaosiedu.mediarecorder.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;

import com.gaosiedu.mediarecorder.egl.EGLSurfaceView;
import com.gaosiedu.mediarecorder.listener.OnFBOTextureIdChangedListener;
import com.gaosiedu.mediarecorder.render.BaseEGLRender;
import com.gaosiedu.mediarecorder.render.CameraFBORender;
import com.gaosiedu.mediarecorder.shader.PROGRAM;

import java.nio.ByteBuffer;

public class CameraPreviewView extends EGLSurfaceView {

    private CameraFBORender fboRender;
    private CCamera camera;


    private int cameraId = 0;

    private int textureId;

    private int width;
    private int height;


    private OnTakePhotoListener onTakePhotoListener;

    private OnFBOTextureIdChangedListener onFBOTextureIdChangedListener;

    public CameraPreviewView(Context context, int width, int height) {
        this(context, null, width, height);

    }

    public CameraPreviewView(Context context, AttributeSet attrs, int width, int height) {
        this(context, attrs, 0, width, height);
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr, int width, int height) {
        super(context, attrs, defStyleAttr);
        init(context, width, height);
    }

    private void init(Context context, int width, int height) {

        this.width = width;
        this.height = height;


        fboRender = new CameraFBORender(context, width, height);

        fboRender.setOnTakePhotoListener(this::getBitmap);

        fboRender.setOnFBOTextureIdChangedListener(id -> {
            if (onFBOTextureIdChangedListener != null) {
                onFBOTextureIdChangedListener.onFBOTextureIdChanged(id);
            }
        });

        camera = new CCamera(context);

        previewAngle(context);

        fboRender.setOnSurfaceCreatedListener((surfaceTexture, textureId) -> {
            camera.initCamera(surfaceTexture, cameraId);
            this.textureId = textureId;
        });


        setRender(fboRender);
        setRenderMode(RenderMode.RENDER_MODE_CONTINUOUSLY);


    }

    public void release() {
        if (camera != null) {
            camera.stopPreview();
        }
    }


    public void previewAngle(Context context) {

        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        fboRender.resetMatrix();

        switch (angle) {


            case Surface.ROTATION_0:

                Log.e("camera", "0");

                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    fboRender.setAngle(90, 0, 0, 1);
                    fboRender.setAngle(180, 1, 0, 0);
                } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    fboRender.setAngle(90, 0, 0, 1);
                }


                break;

            case Surface.ROTATION_90:

                Log.e("camera", "90");

                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    fboRender.setAngle(180, 1, 0, 0);
                } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    fboRender.setAngle(180, 0, 0, 1);
                }


                break;

            case Surface.ROTATION_180:

                Log.e("camera", "180");

                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {


                } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {


                }


                break;

            case Surface.ROTATION_270:

                Log.e("camera", "270");

                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    fboRender.setAngle(180, 0, 1, 0);
                } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                }


                break;

        }

    }

    public int getTextureId() {
        return fboRender.getFboTextureId();
    }

    public BaseEGLRender getPreviewRender() {
        return fboRender.getPreviewRender();
    }

    public void setStickers(Bitmap b1, Bitmap b2) {
        fboRender.setStickers(b1, b2);
    }

    public void setFragmentShader(PROGRAM p) {
        fboRender.setFragmentShader(p);
    }

    public void switchCamera(int cameraId) {
        this.cameraId = cameraId;
        camera.switchCamera(cameraId);
    }

    public void setOnFBOTextureIdChangedListener(OnFBOTextureIdChangedListener onFBOTextureIdChangedListener) {
        this.onFBOTextureIdChangedListener = onFBOTextureIdChangedListener;
    }

    public void takePhoto() {
        fboRender.takePhoto(cameraId);
    }

    private void getBitmap(byte[] buffer) {
        new Thread(() -> {

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);


            ByteBuffer b = ByteBuffer.wrap(buffer);
            bitmap.copyPixelsFromBuffer(b);

            Matrix matrix = new Matrix();
            matrix.setScale(1, -1);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

            if (onTakePhotoListener != null) {
                onTakePhotoListener.onTake(bitmap);
            }

        }).start();
    }


    private boolean isNeedFocus = true; //是否需要聚焦
    final long FLAG_MAX_DURATION = 1000;
    final long FLAG_MAX_SPACE = 50;
    long downTime = 0;
    float startX;
    float startY;
    private int focusX;     //聚焦的x坐标
    private int focusY;     //聚焦的y坐标

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isNeedFocus) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTime = System.currentTimeMillis();
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                long duration = System.currentTimeMillis() - downTime;
                float space = Math.max(Math.abs(event.getX() - startX), Math.abs(event.getY() - startY));
//                Log.d("wys","[onTouchEvent]  duration:"+duration+"  space:"+space);
                if (duration < FLAG_MAX_DURATION && space < FLAG_MAX_SPACE) {
                    focusX = (int) event.getX();
                    focusY = (int) event.getY();
                    camera.newFocus(focusX, focusY);
                }
                break;
        }
        return true;

    }

    public void pausePreview() {
        if (camera != null) {
            camera.pausePreview();
        }
    }

    public void resumePreview() {
        if (camera != null) {
            camera.resumePreview();
        }
    }


    public void setOnTakePhotoListener(OnTakePhotoListener onTakePhotoListener) {
        this.onTakePhotoListener = onTakePhotoListener;
    }

    public interface OnTakePhotoListener {
        void onTake(Bitmap bitmap);
    }

    public void setFocusListener(CCamera.IFocusListener listener) {
        camera.setFocusListener(listener);
    }

    public void playSoundEffect(Activity activity){

        RingtoneManager rm = new RingtoneManager(activity);

        Ringtone ringtone = rm.getRingtone(activity,Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));

        ringtone.play();

    }

}
