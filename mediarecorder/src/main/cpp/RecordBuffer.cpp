//
// Created by Godrick Crown on 2018/12/21.
//

#include <cstddef>
#include "RecordBuffer.h"

RecordBuffer::RecordBuffer(int bufferSize) {

    buffer = new short *[2];

    for(int i = 0; i < 2; i ++)
    {
        buffer[i] = new short[bufferSize];
    }
}

RecordBuffer::~RecordBuffer() {
    for(int i = 0; i < 2; i ++)
    {
        delete buffer[i];
        buffer[i] = NULL;
    }

    delete buffer;
    buffer = NULL;
}

short *RecordBuffer::getRecordBuffer() {
    this->index++;

    if(index > 1)
    {
        index = 1;
    }


    return buffer[index];
}

short *RecordBuffer::getCacheBuffer() {
    return buffer[index];
}
