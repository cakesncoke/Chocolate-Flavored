#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;
out vec4 fragColor;

void main() {
    // Emissive reveal: neither terrain lighting nor environmental fog dims the border.
    fragColor = vertexColor * ColorModulator;
}
