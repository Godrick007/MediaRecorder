package com.gaosiedu.mediarecorder.encoder;

import android.content.Context;

import com.gaosiedu.mediarecorder.render.BaseEGLRender;


public class MediaEncode extends BaseMediaEncoder {

    private BaseEGLRender encodeRender;


    public MediaEncode(Context context, int textureId) {
        super(context);
        encodeRender = new EncodeRender(context,textureId);
        setRender(encodeRender);
        setRenderMode(RenderMode.RENDER_MODE_CONTINUOUSLY);
    }
}
