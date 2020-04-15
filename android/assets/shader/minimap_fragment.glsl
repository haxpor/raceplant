precision mediump float;

varying vec4 v_color;
varying vec2 v_texCoords;

// information packed inside v_applyGrayScale
// - Flag to do gray scale
//      1.0 = gray scale, 0.0 = normal color
// - Type of tilemap's layer
//      0.1 = floor, 0.2 = water, 0.3 = stockpiles, 0.4 = plantslot
uniform vec2 u_applyGrayScale;

uniform sampler2D u_texture;
uniform mat4 u_projTrans;

vec4 grayScale() {
    vec4 color = v_color * texture2D(u_texture, v_texCoords);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 grayScale = vec3(gray);

    return vec4(grayScale, color.a * (256.0/255.0));
}

vec4 normalColor() {
   vec4 color = v_color * texture2D(u_texture, v_texCoords);

   // floor
   if (u_applyGrayScale.g - 1.0 < 0.0001) {
      return vec4(237.0/255.0, 203.0/255.0, 179.0/255.0, color.a * (256.0/255.0));
   }
   // water
   else if (u_applyGrayScale.g - 2.0 < 0.0001) {
      return vec4(89.0/255.0, 182.0/255.0, 227.0/255.0, color.a * (256.0/255.0));
   }
   // stockpiles
   else if (u_applyGrayScale.g - 3.0 < 0.0001) {
      return vec4(191.0/255.0, 150.0/255.0, 91.0/255.0, color.a * (256.0/255.0));
   }
   // plantslot
   else if (u_applyGrayScale.g - 4.0 < 0.0001) {
      return vec4(123.0/255.0, 193.0/255.0, 99.0/255.0, color.a * (256.0/255.0));
   }
   // should not happen, we just return normal color
   else {
      return vec4(color.rgb, color.a * (256.0/255.0));
   }
}

void main() {

    if (u_applyGrayScale.r == 1.0) {
        gl_FragColor = grayScale();
    }
    else {
        gl_FragColor = normalColor();
    }
}