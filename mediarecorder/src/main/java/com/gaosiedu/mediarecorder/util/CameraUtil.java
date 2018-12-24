package com.gaosiedu.mediarecorder.util;

import android.content.Context;
import android.hardware.Camera;

public class CameraUtil {



    public static Camera.Size getCameraSize(Context context){

        int screenHeight = DisplayUtil.getScreenHeight(context);

        Camera camera = Camera.open();

        for(int i = 0; i < camera.getParameters().getSupportedPreviewSizes().size();i++){

//            Log.e(
//                    "camera",
//                    String.format("width is %d, height is %d",
//                            camera.getParameters().getSupportedPreviewSizes().get(i).width,
//                            camera.getParameters().getSupportedPreviewSizes().get(i).height
//                    )
//            );
            Camera.Size size = camera.getParameters().getSupportedPreviewSizes().get(i);

            if(screenHeight == size.height){

                if(size.height / size.width == 9 / 16){
                    camera.release();
                    return size;
                }

            }else{

                if(size.height / size.width ==  9 / 16){
                    camera.release();
                    return size;
                }

            }


        }

        Camera.Size size = camera.getParameters().getSupportedPreviewSizes().get(0);

        camera.release();

        return size;
    }

}
