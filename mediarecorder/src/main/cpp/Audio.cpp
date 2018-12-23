//
// Created by Godrick Crown on 2018/12/21.
//


#include "Audio.h"

Audio::Audio(CallBack2Java *callJava) {
    this->callJava = callJava;
    this->recordBuffer = new RecordBuffer(RECORD_BUFFER_SIZE);
}

Audio::~Audio() {

    slObject = NULL;
    slEngine = NULL;
    recorder = NULL;
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
    Audio *audio = static_cast<Audio *>(context);

    if(audio->isRecording)
    {
        if(audio->callJava && audio->recordBufferQueue)
        {
            if(audio->callJava)
            {
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

    SLresult result;

    slCreateEngine(&this->slObject,0,NULL,0,NULL,NULL);

    (*slObject)->Realize(slObject,SL_BOOLEAN_FALSE);

    (*slObject)->GetInterface(slObject,SL_IID_ENGINE,&this->slEngine);

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


    (*slEngine)->CreateAudioRecorder(
            slEngine,
            &this->recordObject,
            &audioSource,
            &audioSink,
            1,
            id,
            req
            );

    (*recordObject)->Realize(recordObject,SL_BOOLEAN_FALSE);

    (*recordObject)->GetInterface(
            recordObject,
            SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
            &this->recordBufferQueue
    );

    (*recordBufferQueue)->Enqueue(
            recordBufferQueue,
            this->recordBuffer->getRecordBuffer(),
            RECORD_BUFFER_SIZE
            );

    (*recordBufferQueue)->RegisterCallback(recordBufferQueue,recordBufferCallback,this);

    (*recordObject)->GetInterface(recordObject,SL_IID_RECORD,&this->recorder);

    (*recorder)->SetRecordState(recorder,SL_RECORDSTATE_RECORDING);

}
