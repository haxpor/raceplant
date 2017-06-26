#ifdef GL_ES
#define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_applyGrayScale;

uniform sampler2D u_texture;
uniform mat4 u_projTrans;

vec4 grayScale() {
    vec4 color = v_color * texture2D(u_texture, v_texCoords);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 grayScale = vec3(gray);

    return vec4(grayScale, color.a * (256.0/255.0) * 0.9);
}

vec4 normalColor() {
   return v_color * texture2D(u_texture, v_texCoords);
}

void main() {

    if (v_applyGrayScale.r == 1.0) {
        gl_FragColor = grayScale();
    }
    else {
        gl_FragColor = normalColor();
    }
}