#version 330

uniform int renderMode = 0;
uniform sampler2D u_texture;
uniform int textureScale = 1;

in vec4 Position;
in vec4 WorldPosition;
in vec2 TexCoords;

out vec4 FinalColour;

void main() {

	switch(renderMode) {
	    case 0:
    	    FinalColour = texture(u_texture, mod(Position.xy * textureScale, 1.0));
    	    break;
	    case 1:
	        FinalColour = texture(u_texture, mod(TexCoords * textureScale, 1.0));
	        break;
	    case 2:
	        FinalColour = vec4(Position.xyz, 1.0);
	        break;
	    case 3:
	        FinalColour = vec4(WorldPosition.xyz, 1.0);
	        break;
	    case 4:
	        FinalColour = vec4(TexCoords, 0.0, 1.0);
	        break;
	    default:
	        FinalColour = vec4(1.0, 0.0, 0.0, 1.0);
	        break;
	}

}
