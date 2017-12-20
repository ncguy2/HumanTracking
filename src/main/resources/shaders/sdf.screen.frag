#version 330

in vec2 TexCoords;

uniform sampler2D u_texture;

out vec4 FinalColour;

void main() {
	vec4 col = texture(TexCoords, u_texture);
	FinalColour = vec4(col.rgb, 1.0);
}
