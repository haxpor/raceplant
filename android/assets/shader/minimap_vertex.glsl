attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec2 a_applyGrayScale;

uniform mat4 u_projTrans;
uniform float u_applyGrayScale; // 1.0 = enable, 0.0 = disable

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_applyGrayScale;

void main() {
    v_color = a_color;
    v_texCoords = a_texCoord0;
    v_applyGrayScale = a_applyGrayScale;
    gl_Position = u_projTrans * a_position;
}