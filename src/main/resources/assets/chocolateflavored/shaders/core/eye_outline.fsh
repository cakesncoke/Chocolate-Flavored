#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

layout(location = 2) in vec4 vertexColor;
layout(location = 0) out vec4 fragColor;

void main() {
    // Emissive reveal: neither terrain lighting nor environmental fog dims the border.
    fragColor = vertexColor * ColorModulator;
}
