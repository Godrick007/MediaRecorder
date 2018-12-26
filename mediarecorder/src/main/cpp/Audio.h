//
// Created by Godrick Crown on 2018/12/21.
//

#ifndef MEDIARECORD_AUDIO_H
#define MEDIARECORD_AUDIO_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <cwchar>
#include <pthread.h>
#include "CallBack2Java.h"
#include "RecordBuffer.h"
#include "androidLog.h"


#define RECORD_BUFFER_SIZE 4096 * 2

class Audio {

public:

    SLObjectItf slObject;
    SLEngineItf slEngine;
    SLObjectItf recordObject;
    SLRecordItf recorder;
    SLAndroidSimpleBufferQueueItf  recordBufferQueue = NULL;

    pthread_t thread_record;

    CallBack2Java *callJava;
    RecordBuffer *recordBuffer;

    bool isRecording = false;
    bool isExit = false;

public:

    Audio(CallBack2Java *callJava);
    ~Audio();

    void startMICRecord();
    void stopMICRecord();


    void initRecord();

};


#endif //MEDIARECORD_AUDIO_H
