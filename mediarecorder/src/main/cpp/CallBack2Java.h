//
// Created by Godrick Crown on 2018/12/21.
//

#ifndef MEDIARECORD_CALLBACK2JAVA_H
#define MEDIARECORD_CALLBACK2JAVA_H

#include <cwchar>
#include "jni.h"

class CallBack2Java {

public:
    JavaVM *jvm = NULL;
    JNIEnv *env = NULL;
    jobject jobj;

    jmethodID mid_pcm_calback;

public:
    CallBack2Java(JavaVM *jvm, JNIEnv *env,jobject *obj);

    ~CallBack2Java();

    void callback2JavaPCMDataCallback(const void *buffer, int size);
};


#endif //MEDIARECORD_CALLBACK2JAVA_H
