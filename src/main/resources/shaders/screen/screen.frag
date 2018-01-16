#version 330

out vec4 FinalColour;

uniform sampler2D u_texture;
uniform sampler2D u_depthTex;

in vec4 Colour;
in vec2 TexCoords;
in vec3 Position;
in vec4 WorldPosition;

void main() {
    FinalColour = texture2D(u_depthTex, TexCoords);
}