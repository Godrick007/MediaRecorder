package com.gaosiedu.mediarecorder.encoder;

import android.content.Context;
import android.graphics.Bitmap;

import com.gaosiedu.mediarecorder.render.BaseEGLRender;
import com.gaosiedu.mediarecorder.render.EncodeRender;


public class MediaEncode extends BaseMediaEncoder {

    private EncodeRender encodeRender;


    public MediaEncode(Context context, int textureId) {
        super(context);
        encodeRender = new EncodeRender(context,textureId);
        setRender(encodeRender);
        setRenderMode(RenderMode.RENDER_MODE_CONTINUOUSLY);
    }


    public void setStickers(Bitmap b1, Bitmap b2){
        ((EncodeRender)encodeRender).addSticker1(b1);
        ((EncodeRender)encodeRender).addSticker2(b2);
    }

    public void setFBOTextureId(int textureId){
        encodeRender.setFBOId(textureId);
    }

}
