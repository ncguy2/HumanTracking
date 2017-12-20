#version 330

in vec4 a_position;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;

out vec3 Position;
out vec2 TexCoords;

void main() {
    Position = a_position.xyz;
    TexCoords = a_texCoord0;
	gl_Position = u_projTrans * a_position;
}
