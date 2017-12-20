#version 330

in vec4 a_position;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;

out vec4 Position;
out vec4 WorldPosition;
out vec2 TexCoords;

void main() {
    TexCoords = a_texCoord0;
    Position = a_position;

    vec4 pos = u_projTrans * a_position;

    WorldPosition = pos;
    gl_Position = pos;
}