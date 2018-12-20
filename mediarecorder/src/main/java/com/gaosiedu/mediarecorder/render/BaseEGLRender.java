package com.gaosiedu.mediarecorder.render;


public abstract class BaseEGLRender {




    public void onSurfaceCreated(){
        onCreated();
        onCreatedExtra();
    }

    public void onSurfaceChanged(int width,int height){
        onChange(width,height);
    }

    public void onSurfaceDrawFrame(){
        onDrawFrame();
        onDrawExtra();
    }




    //implements when needed

    protected void initExtra(){

    }

    protected abstract void onCreated();

    protected abstract void onChange(int width,int height);

    protected abstract void onDrawFrame();

    protected void onCreatedExtra(){

    }

    protected void onDrawExtra(){

    }

}
