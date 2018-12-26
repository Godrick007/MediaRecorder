//
// Created by Godrick Crown on 2018/12/21.
//


#include "Audio.h"

Audio::Audio(CallBack2Java *callJava) {
    this->isExit = false;
    this->callJava = callJava;
    this->recordBuffer = new RecordBuffer(RECORD_BUFFER_SIZE);
}

Audio::~Audio() {

    slObject = NULL;
    slEngine = NULL;
    recorder = NULL;
    (*recordObject)->Destroy(recordObject);
    recordObject = NULL;
    recordBufferQueue = NULL;

    delete recordBuffer;
    recordBuffer = NULL;
    callJava = NULL;


}

void *recordThread(void *context){

    Audio *audio = static_cast<Audio *>(context);

    audio->initRecord();

    return 0;
}

void Audio::startMICRecord() {

    if(!isExit)
    {
        LOGE("audio recorder","startMic");
        this->isRecording = true;
        pthread_create(&thread_record,NULL,recordThread,this);
    }


}

void Audio::stopMICRecord() {
    this->isExit = true;
    this->isRecording = false;
}

void recordBufferCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    LOGE("audio recorder","recordBufferCallback");

    Audio *audio = static_cast<Audio *>(context);

    if(audio->isRecording)
    {
        if(audio->callJava && audio->recordBufferQueue)
        {
            if(audio->callJava)
            {

                LOGE("audio recorder","record callback to java at cpp");

                audio->callJava->callback2JavaPCMDataCallback(
                        audio->recordBuffer->getCacheBuffer(),

                        RECORD_BUFFER_SIZE
                );
            }

            if(audio->recordBufferQueue)
            {
                (*audio->recordBufferQueue)->Enqueue(
                        audio->recordBufferQueue,
                        audio->recordBuffer->getRecordBuffer(),
                        RECORD_BUFFER_SIZE
                );
            }


        }

    }
    else
    {
        if(audio->recorder)
        {

            (*audio->recorder)->SetRecordState(
                    audio->recorder,
                    SL_RECORDSTATE_STOPPED
            );

        }
    }

}


void Audio::initRecord() {

    LOGE("audio recorder","initRecord");

    SLresult result;

    result = slCreateEngine(&this->slObject,0,NULL,0,NULL,NULL);

    LOGE("audio recorder","result slCreateEngine %d",result);

    (void)result;

    result = (*slObject)->Realize(slObject,SL_BOOLEAN_FALSE);

    LOGE("audio recorder","result obj Realize %d",result);

    (void)result;

    result = (*slObject)->GetInterface(slObject,SL_IID_ENGINE,&this->slEngine);

    LOGE("audio recorder","result obj GetInterface %d",result);

    (void)result;

    SLDataLocator_IODevice loc_dev = {
            SL_DATALOCATOR_IODEVICE,
            SL_IODEVICE_AUDIOINPUT,
            SL_DEFAULTDEVICEID_AUDIOINPUT,
            NULL
    };


    SLDataSource audioSource = {&loc_dev,NULL};

    SLDataLocator_AndroidSimpleBufferQueue loc_bufferQueue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            2
    };

    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM,
            2,
            SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN
    };

    SLDataSink audioSink = {
            &loc_bufferQueue,
            &format_pcm
    };

    SLInterfaceID  id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    SLboolean req[1] = {SL_BOOLEAN_TRUE};


    result = (*slEngine)->CreateAudioRecorder(
            slEngine,
            &this->recordObject,
            &audioSource,
            &audioSink,
            1,
            id,
            req
            );

    LOGE("audio recorder","result obj CreateAudioRecorder %d",result);

    (void)result;

    result = (*recordObject)->Realize(recordObject,SL_BOOLEAN_FALSE);

    LOGE("audio recorder","result record Realize %d",result);

    (void)result;

    result = (*recordObject)->GetInterface(
            recordObject,
            SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
            &this->recordBufferQueue
    );

    LOGE("audio recorder","result record GetInterface %d",result);

    (void)result;

    result = (*recordBufferQueue)->Enqueue(
            recordBufferQueue,
            this->recordBuffer->getRecordBuffer(),
            RECORD_BUFFER_SIZE
            );

    LOGE("audio recorder","result record Enqueue %d",result);

    (void)result;

    result = (*recordBufferQueue)->RegisterCallback(recordBufferQueue,recordBufferCallback,this);

    LOGE("audio recorder","result record RegisterCallback %d",result);

    (void)result;

    result = (*recordObject)->GetInterface(recordObject,SL_IID_RECORD,&this->recorder);

    LOGE("audio recorder","result record GetInterface %d",result);

    (void)result;

    result = (*recorder)->SetRecordState(recorder,SL_RECORDSTATE_RECORDING);

    LOGE("audio recorder","result record SetRecordState %d",result);

    (void)result;

}
