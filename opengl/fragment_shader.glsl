precision mediump float;
uniform sampler2D uTexture;
varying vec2 vTexCoord;

void main() {
    vec4 color = texture2D(uTexture, vTexCoord);

    // Apply edge enhancement
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 edge = vec3(gray);

    // Mix original and edge
    gl_FragColor = vec4(mix(color.rgb, edge, 0.3), color.a);
}