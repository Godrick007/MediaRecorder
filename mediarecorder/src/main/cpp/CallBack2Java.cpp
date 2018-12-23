//
// Created by Godrick Crown on 2018/12/21.
//

#include "CallBack2Java.h"

CallBack2Java::CallBack2Java(JavaVM *jvm, JNIEnv *env, jobject *obj) {

    this->jvm = jvm;
    this->env = env;
    this->jobj = env->NewGlobalRef(*obj);

    jclass clz = env->GetObjectClass(*obj);

    if(!clz){
        return;
    }

    mid_pcm_calback = env->GetMethodID(clz,"nativeCallbackPCMData","([BI)V");

}

CallBack2Java::~CallBack2Java() {

}

void CallBack2Java::callback2JavaPCMDataCallback(const void *buffer, int size) {

    JNIEnv *env;
    if(jvm->AttachCurrentThread(&env, 0) != JNI_OK){
        return;
    }

    jbyteArray data = env->NewByteArray(size);

    env->SetByteArrayRegion(data, 0, size, static_cast<const jbyte *>(buffer));

    env->CallVoidMethod(jobj,mid_pcm_calback,data,size);

    env->DeleteLocalRef(data);

    jvm->DetachCurrentThread();
}
