#include <jni.h>
#include <string>
#include "androidLog.h"


#include "CallBack2Java.h"
#include "Audio.h"

bool exit = true;

JavaVM *javaVm = NULL;
CallBack2Java *calljava = NULL;
Audio *audio = NULL;

pthread_t thread_start_record;


extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    jint result = -1;
    javaVm = vm;

    JNIEnv *env;
    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    LOGE("audio recorder","jvm onLoad");

    return JNI_VERSION_1_4;

}


void *startRecord(void *data)
{
    LOGE("audio recorder","start thread");
    audio->startMICRecord();
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_gaosiedu_mediarecorder_audio_AudioRecord_native_1start_1record(JNIEnv *env,
                                                                        jobject instance) {

    LOGE("audio recorder","start");

    if(!calljava)
    {
        calljava = new CallBack2Java(javaVm,env,&instance);
    }

    if(!audio)
    {
        audio = new Audio(calljava);
    }

    pthread_create(&thread_start_record,NULL,startRecord,NULL);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_gaosiedu_mediarecorder_audio_AudioRecord_native_1stop_1record(JNIEnv *env,
                                                                       jobject instance) {

    if(audio)
    {
        audio->stopMICRecord();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_gaosiedu_mediarecorder_audio_AudioRecord_native_1release(JNIEnv *env, jobject instance) {

    if(audio)
    {
        delete audio;
        audio = NULL;
    }

    if(calljava)
    {
        delete calljava;
        calljava = NULL;
    }

}