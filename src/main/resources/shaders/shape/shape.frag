#version 330

out vec4 FinalColour;

in vec4 Colour;
in vec2 TexCoords;
in vec4 Position;
in vec4 WorldPosition;

uniform sampler2D u_sampler0;
uniform sampler2D u_depth;

uniform float u_near;
uniform float u_far;

void main() {
    float z = (gl_FragCoord.z / gl_FragCoord.w) / u_far;
    float depth = 1.0 - (texture2D(u_depth, TexCoords).r * .1);
    float fragDepth = 1.0 - (WorldPosition.z / u_far);
//    FinalColour = (Colour * texture2D(u_sampler0, TexCoords));

    bool b = depth > z;
    float r = 0.0;
    if(b)
        r = 1.0;

    vec3 ndcPos = WorldPosition.xyz;

    float ndcDepth = ndcPos.z =
        (2.0 * gl_FragCoord.z - gl_DepthRange.near - gl_DepthRange.far) /
        (gl_DepthRange.far - gl_DepthRange.near);
    float clipDepth = ndcDepth / gl_FragCoord.w;


//    FinalColour.rgb = vec3(r, depth, z);
    FinalColour.rgb = vec3((clipDepth * 0.5) + 0.5);
    FinalColour.a = 1.0;
//    FinalColour.rgb = depth - FinalColour.rgb;
}