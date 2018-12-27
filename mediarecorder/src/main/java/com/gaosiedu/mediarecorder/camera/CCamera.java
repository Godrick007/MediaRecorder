package com.gaosiedu.mediarecorder.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class CCamera {

    private static Handler handler = new Handler(Looper.getMainLooper());

    private SurfaceTexture surfaceTexture;
    private Camera camera;

    private Camera.Parameters mCameraParameters;

    private int width;
    private int height;


    public CCamera(Context context) {
    }


    public void initCamera(SurfaceTexture surfaceTexture, int cameraId) {
        this.surfaceTexture = surfaceTexture;
        setCameraParam(cameraId);
    }


    private void setCameraParam(int cameraId) {


        try {
            camera = Camera.open(cameraId);
            camera.setPreviewTexture(surfaceTexture);

            mCameraParameters = camera.getParameters();

            mCameraParameters.setFlashMode("off");
            mCameraParameters.setPreviewFormat(ImageFormat.NV21);

            Camera.Size sizePicture = getFitSize(mCameraParameters.getSupportedPictureSizes());
            Camera.Size sizePreview = getFitSize(mCameraParameters.getSupportedPreviewSizes());

            this.width = sizePreview.width;
            this.height = sizePreview.height;

            handler.postDelayed(() -> {
                newFocus(width / 2, height / 2);
            }, 1000);

            mCameraParameters.setPreviewSize(
                    sizePreview.width,
                    sizePreview.height
            );


            mCameraParameters.setPictureSize(
                    sizePicture.width,
                    sizePicture.height
            );

            camera.setParameters(mCameraParameters);

            camera.startPreview();


        } catch (Exception e) {
            e.printStackTrace();
            stopPreview();
        }

    }

    public void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void switchCamera(int cameraId) {

        if (camera != null) {
            stopPreview();
        }

        setCameraParam(cameraId);

    }


    private Camera.Size getFitSize(List<Camera.Size> sizes) {

        if (width < height) {

            int t = height;
            height = width;
            width = t;

        }

        for (Camera.Size size : sizes) {

            if (1.0f * size.width / size.height == 1.0f * width / height) {
                return size;
            }

        }

        return sizes.get(0);
    }

    public boolean newFocus(int x, int y) {
        //正在对焦时返回
        if (camera == null) {
            return false;
        }
        setMeteringRect(x, y);
        mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
        try {
            camera.setParameters(mCameraParameters);
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
//                    App.getHandler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            //一秒之后才能再次对焦
//                            sensorController.unlockFocus();
//                        }
//                    }, 500);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 设置感光区域
     * 需要将屏幕坐标映射到Rect对象对应的单元格矩形
     *
     * @param x
     * @param y
     */
    private void setMeteringRect(int x, int y) {
        if (mCameraParameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            Rect rect = new Rect(x - 100, y - 100, x + 100, y + 100);
            int left = rect.left * 2000 / width - 1000;
            int top = rect.top * 2000 / height - 1000;
            int right = rect.right * 2000 / width - 1000;
            int bottom = rect.bottom * 2000 / height - 1000;
            // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            Rect area1 = new Rect(left, top, right, bottom);
            if (listener != null) {
                listener.onFocus(rect);
            }
            //只有一个感光区，直接设置权重为1000了
            areas.add(new Camera.Area(area1, 1000));
            mCameraParameters.setMeteringAreas(areas);
        }
    }

    private IFocusListener listener;
    public void setFocusListener(IFocusListener listener){
        this.listener=listener;
    }

    public interface IFocusListener{
        void onFocus(Rect rect);
    }

}
