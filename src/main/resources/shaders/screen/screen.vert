#version 330

in vec4 a_position;
in vec4 a_color;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;

out vec4 Colour;
out vec3 Position;
out vec4 WorldPosition;
out vec2 TexCoords;

void main() {
    Colour = a_color;
    TexCoords = a_texCoord0;
    Position = a_position.xyz;
    gl_Position = WorldPosition = u_projTrans * a_position;
}