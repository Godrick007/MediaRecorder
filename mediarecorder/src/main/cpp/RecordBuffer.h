//
// Created by Godrick Crown on 2018/12/21.
//

#ifndef MEDIARECORD_RECORDBUFFER_H
#define MEDIARECORD_RECORDBUFFER_H


class RecordBuffer {

public:
    short **buffer;
    int index = -1;


public:
    RecordBuffer(int bufferSize);
    ~RecordBuffer();

    short *getRecordBuffer();

    short *getCacheBuffer();
};


#endif //MEDIARECORD_RECORDBUFFER_H
