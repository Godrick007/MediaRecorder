package com.gaosiedu.mediarecorder.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.gaosiedu.mediarecorder.egl.EGLSurfaceView;
import com.gaosiedu.mediarecorder.render.BaseEGLRender;
import com.gaosiedu.mediarecorder.render.CameraFBORender;
import com.gaosiedu.mediarecorder.shader.PROGRAM;

import javax.microedition.khronos.opengles.GL;

public class CameraPreviewView extends EGLSurfaceView {

    private CameraFBORender fboRender;
    private CCamera camera;

    private int cameraId = 0;

    private int textureId;

    private int width;
    private int height;

    public CameraPreviewView(Context context,int width,int height) {
        this(context,null,width,height);

    }

    public CameraPreviewView(Context context, AttributeSet attrs,int width,int height) {
        this(context, attrs,0,width,height);
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr,int width,int height) {
        super(context, attrs, defStyleAttr);
        init(context,width,height);
    }

    private void init(Context context,int width,int height) {

        this.width = width;
        this.height = height;


        fboRender = new CameraFBORender(context,width,height);
        camera = new CCamera(context);

        previewAngle(context);

        fboRender.setOnSurfaceCreatedListener((surfaceTexture , textureId)-> {
            camera.initCamera(surfaceTexture,cameraId);
            this.textureId = textureId;
        });

        setRender(fboRender);
        setRenderMode(RenderMode.RENDER_MODE_CONTINUOUSLY);


    }

    public void onDestory(){
        if(camera != null){
            camera.stopPreview();
        }
    }


    public void previewAngle(Context context){

        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        fboRender.resetMatrix();

        switch (angle){



            case Surface.ROTATION_0:

                Log.e("camera","0");

                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    fboRender.setAngle(90,0,0,1);
                    fboRender.setAngle(180,1,0,0);
                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    fboRender.setAngle(90,0,0,1);
                }



                break;

            case Surface.ROTATION_90:

                Log.e("camera","90");

                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    fboRender.setAngle(180,1,0,0);
                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    fboRender.setAngle(180,0,0,1);
                }


                break;

            case Surface.ROTATION_180:

                Log.e("camera","180");

                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){



                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){



                }


                break;

            case Surface.ROTATION_270:

                Log.e("camera","270");

                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    fboRender.setAngle(180,0,1,0);
                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){

                }


                break;

        }

    }

    public int getTextureId(){
        return textureId;
    }

    public BaseEGLRender getPreviewRender(){
        return fboRender.getPreviewRender();
    }

    public void setStickers(Bitmap b1,Bitmap b2){
        fboRender.setStickers(b1,b2);
    }

    public void setFragmentShader(PROGRAM p){
        fboRender.setFragmentShader(p);
    }

    public void switchCamera(int cameraId){
        this.cameraId = cameraId;
        camera.switchCamera(cameraId);
    }

}
