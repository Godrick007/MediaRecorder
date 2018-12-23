#extension GL_OES_EGL_image_external:require
precision mediump float;
varying vec2 ft_Position;
uniform samplerExternalOES sTexture;
float colorMatrix[20];
void main(){

    colorMatrix[0] = 0.9;
    colorMatrix[1] = 0.0;
    colorMatrix[2] = 0.0;
    colorMatrix[3] = 0.0;
    colorMatrix[4] = 0.0;


    colorMatrix[5] = 0.0;
    colorMatrix[6] = 1.1;
    colorMatrix[7] = 0.0;
    colorMatrix[8] = 0.0;
    colorMatrix[9] = 0.0;

    colorMatrix[10] = 0.0;
    colorMatrix[11] = 0.0;
    colorMatrix[12] = 0.9;
    colorMatrix[13] = 0.0;
    colorMatrix[14] = 0.0;

    colorMatrix[15] = 0.0;
    colorMatrix[16] = 0.0;
    colorMatrix[17] = 0.0;
    colorMatrix[18] = 1.0;
    colorMatrix[19] = 0.0;



    vec4 rgb = texture2D(sTexture,ft_Position);

    float rn = rgb.r * colorMatrix[0 + 5 * 0] + rgb.g * colorMatrix[1 + 5 * 0] + rgb.b * colorMatrix[2 + 5 * 0] + rgb.a * colorMatrix[3 + 5 * 0] + colorMatrix[4 + 5 * 0];
    float gn = rgb.r * colorMatrix[0 + 5 * 1] + rgb.g * colorMatrix[1 + 5 * 1] + rgb.b * colorMatrix[2 + 5 * 1] + rgb.a * colorMatrix[3 + 5 * 1] + colorMatrix[4 + 5 * 1];
    float bn = rgb.r * colorMatrix[0 + 5 * 2] + rgb.g * colorMatrix[1 + 5 * 2] + rgb.b * colorMatrix[2 + 5 * 2] + rgb.a * colorMatrix[3 + 5 * 2] + colorMatrix[4 + 5 * 2];
    float an =   0.0 * colorMatrix[0 + 5 * 3] +   0.0 * colorMatrix[1 + 5 * 3] +   0.0 * colorMatrix[2 + 5 * 3] +   1.0 * colorMatrix[3 + 5 * 3] + colorMatrix[4 + 5 * 3];

    gl_FragColor = vec4(rn,gn,bn,an);

}