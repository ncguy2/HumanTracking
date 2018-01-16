#version 330

in vec4 a_position;
in vec4 a_color;
in vec2 a_texCoord0;

uniform mat4 u_projModelView;

out vec4 Colour;
out vec2 TexCoords;
out vec4 Position;
out vec4 WorldPosition;

void main() {
    Colour = a_color;
    TexCoords = a_texCoord0;

    Position = a_position;

    gl_Position = WorldPosition = u_projModelView * a_position;
    gl_PointSize = 1.0;
}