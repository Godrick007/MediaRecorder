package com.gaosiedu.mediarecorder.audio;

import com.gaosiedu.mediarecorder.listener.OnNativeCallbackPCMDataListener;

public class AudioRecord {

    static{
        System.loadLibrary("MediaRecorder");
    }

    private OnNativeCallbackPCMDataListener onNativeCallbackPCMDataListener;







    public void setOnNativeCallbackPCMDataListener(OnNativeCallbackPCMDataListener onNativeCallbackPCMDataListener) {
        this.onNativeCallbackPCMDataListener = onNativeCallbackPCMDataListener;
    }


    //

    public void startRecord(){
        native_start_record();
    }

    public void stopRecord(){
        native_stop_record();
    }

    public void release(){native_release();}


    //native functions begin


    private native void native_start_record();

    private native void native_stop_record();

    private native void native_release();

    //native functions end

    //===================================================================================================

    //native callbacks begin

    public void nativeCallbackPCMData(byte[] buffer,int size){
        if(onNativeCallbackPCMDataListener != null){
            onNativeCallbackPCMDataListener.onCallbackPCMData(buffer,size);
        }
    }
    //native callbacks end
}
