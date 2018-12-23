package com.gaosiedu.mediarecorder.listener;

public interface OnNativeCallbackPCMDataListener {

    void onCallbackPCMData(byte[] buffer,int size);

}
