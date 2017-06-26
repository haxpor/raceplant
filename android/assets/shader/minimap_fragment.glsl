#ifdef GL_ES
#define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform mat4 u_projTrans;

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 grayScale = vec3(gray);

    gl_FragColor = vec4(grayScale, color.a * (256.0/255.0) * 0.9);
}