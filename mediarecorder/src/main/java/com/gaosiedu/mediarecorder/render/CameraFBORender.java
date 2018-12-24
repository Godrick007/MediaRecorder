package com.gaosiedu.mediarecorder.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.gaosiedu.mediarecorder.R;
import com.gaosiedu.mediarecorder.listener.OnSurfaceCreatedListener;
import com.gaosiedu.mediarecorder.shader.PROGRAM;
import com.gaosiedu.mediarecorder.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CameraFBORender extends BaseEGLRender implements SurfaceTexture.OnFrameAvailableListener {



    private final float[] vertex_data = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    private final float[] texture_data = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    private final float[] illusion = {
          0.8f,0.3f,0.1f,0.0f,0.18f,
          0.1f,0.9f,0.0f,0.0f,0.18f,
          0.1f,0.3f,0.7f,0.0f,0.18f,
          0.8f,0.3f,0.1f,0.0f,0.0f
    };


    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;


    private int program_current;
    private int program_normal;
    private int program_illusion;
    private int program_cute;
    private int program_refresh;
    private int program_reminiscence;
    private int program_charming;

    private int avPosition;
    private int afPosition;
    private int fboTextureId;
    private int sampler;

    private int VBOId;
    private int FBOId;

    private int u_matrix;
    private float[] matrix = new float[16];


    private int cameraTextureId;

    private SurfaceTexture surfaceTexture;

    private Context context;

    private OnSurfaceCreatedListener onSurfaceCreatedListener;

    private CameraPreviewRender previewRender;


    private int width;
    private int height;

    public CameraFBORender(Context context,int width,int height) {

        this.width = width;
        this.height = height;

        this.context = context;
        vertexBuffer = ByteBuffer.allocateDirect(vertex_data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertex_data);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(texture_data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(texture_data);
        textureBuffer.position(0);

        previewRender = new CameraPreviewRender(context);

    }


    @Override
    protected void onCreated() {

        previewRender.onCreated();
        initProgram();
        program_current = program_normal;
        avPosition = GLES20.glGetAttribLocation(program_normal, "v_Position");
        afPosition = GLES20.glGetAttribLocation(program_normal, "f_Position");
        sampler = GLES20.glGetUniformLocation(program_normal, "sTexture");
        u_matrix = GLES20.glGetUniformLocation(program_normal, "u_Matrix");

        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        VBOId = vbos[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBOId);

        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                vertex_data.length * 4 + texture_data.length * 4,
                null,
                GLES20.GL_STATIC_DRAW
        );

        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                vertex_data.length * 4,
                vertexBuffer
        );

        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                vertex_data.length * 4,
                texture_data.length * 4,
                textureBuffer
        );

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        FBOId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBOId);


        int[] textureIds = new int[1];

        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);


        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                width,
                height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
        );

        //bind texture 2 fbo
        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                fboTextureId,
                0
        );

        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE){
            //failed
            Log.e("opengl","fbo NOT cool");
        }else{
            //success
            Log.e("opengl","fbo cool");
        }


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        int[] textureIdsEos = new int[1];

        GLES20.glGenTextures(1, textureIdsEos, 0);


        cameraTextureId = textureIdsEos[0];

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(cameraTextureId);

        surfaceTexture.setOnFrameAvailableListener(this);

        if (onSurfaceCreatedListener != null) {
            onSurfaceCreatedListener.onSurfaceCreated(surfaceTexture, fboTextureId);
        }

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

    }


    public void onChange(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onDrawFrame() {

        surfaceTexture.updateTexImage();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0f, 0f, 1f);

        GLES20.glUseProgram(program_current);
        GLES20.glViewport(0, 0, width, height);

        GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBOId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBOId);
        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8, vertex_data.length * 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //unbind
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


        previewRender.onChange(width, height);
        previewRender.onDrawFrame(fboTextureId);

    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }


    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void resetMatrix() {
        Matrix.setIdentityM(matrix, 0);
    }


    public void setAngle(float angle, float x, float y, float z) {
        Matrix.rotateM(matrix, 0, angle, x, y, z);
    }

    public void setOnSurfaceCreatedListener(OnSurfaceCreatedListener onSurfaceCreatedListener) {
        this.onSurfaceCreatedListener = onSurfaceCreatedListener;
    }


    public void setStickers(Bitmap b1, Bitmap b2) {
        if (b1 != null) {
            previewRender.addSticker1(b1);
        }
        if (b2 != null) {
            previewRender.addSticker2(b2);
        }
    }

    public BaseEGLRender getPreviewRender() {
        return previewRender;
    }


    public void setFragmentShader(PROGRAM p) {
        switch (p) {
            case CHARMING:
                program_current = program_charming;
                break;
            case NORMAL:
                program_current = program_normal;
                break;
            case CUTE:
                program_current = program_cute;
                break;
            case REFRESH:
                program_current = program_refresh;
                break;
            case ILLUSION:
                program_current = program_illusion;
                break;
            case REMINISCENCE:
                program_current = program_reminiscence;
                break;
            default:
                program_current = program_normal;
                break;

        }
    }


    private void initProgram() {

        String vertexSource = ShaderUtil.readRawText(context, R.raw.vertex2_m);

        String normal = ShaderUtil.readRawText(context, R.raw.fragment_camera_fbo);
        program_normal = ShaderUtil.createProgram(vertexSource, normal);

        String illusion = ShaderUtil.readRawText(context, R.raw.fragment_camera_fbo_illusion);
        program_illusion = ShaderUtil.createProgram(vertexSource, illusion);

        String cute = ShaderUtil.readRawText(context, R.raw.fragment_camera_fbo_cute);
        program_cute = ShaderUtil.createProgram(vertexSource, cute);

        String refresh = ShaderUtil.readRawText(context, R.raw.fragment_camera_fbo_refresh);
        program_refresh = ShaderUtil.createProgram(vertexSource, refresh);

        String reminiscence = ShaderUtil.readRawText(context, R.raw.fragment_camera_fbo_reminiscence);
        program_reminiscence = ShaderUtil.createProgram(vertexSource, reminiscence);

        String charming = ShaderUtil.readRawText(context, R.raw.fragment_camera_fbo_charming);
        program_charming = ShaderUtil.createProgram(vertexSource, charming);
    }


}
