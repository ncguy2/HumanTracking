#version 330

#define MAX_MODELS 16
#define MAX_TREE_NODES 32

#define OPERATION_NONE 0
#define OPERATION_UNION 1
#define OPERATION_INTERSECTION 2
#define OPERATION_DIFFERENCE 3

#pragma include("includes/SDFGeometry.glsl")

uniform int rootItemIndex;
uniform GeometryItem items[MAX_TREE_NODES];
uniform Geometry models[MAX_MODELS];
float itemValues[MAX_TREE_NODES];
bool itemsCalculated[MAX_TREE_NODES];

#pragma include("includes/TreeTraversal.glsl")
#pragma include("includes/ItemEvaluation.glsl")

in vec3 Position;
in vec2 TexCoords;

out vec4 FinalColour;

uniform int maxSteps = 255;
uniform float rayStart = 0.0;
uniform float rayEnd = 100.0;
uniform float epsilon = 0.0001;
uniform vec2 screenSize;
uniform float fov;
uniform vec3 position;
uniform vec3 viewRayDirection;
uniform vec3 up;
uniform float time;
uniform mat4 view;

float SumValues(float curVal, float next, int operationId, out bool usedLeft) {
    switch(operationId) {
        case OPERATION_UNION:
            return UnionSDF(curVal, next, usedLeft);
        case OPERATION_INTERSECTION:
            return IntersectSDF(curVal, next, usedLeft);
        case OPERATION_DIFFERENCE:
            return DifferenceSDF(curVal, next, usedLeft);
    }

    return curVal;
}

float sceneSDF(vec3 samplePoint, out vec4 colour) {

    for(int i = 0; i < MAX_TREE_NODES; i++) {
        GeometryItem item = items[i];
        itemsCalculated[i] = false;
        if(!item.valid || item.data == -1) continue;
        Geometry g = models[item.data];
        itemValues[i] = SDF(samplePoint, g);
        itemsCalculated[i] = true;
    }

    GeometryItem item = items[rootItemIndex];
    return EvaluateItem(item);
}

float GetDepth(vec3 eye, vec3 direction, float start, float end, out float stepRatio, out vec4 colour) {
    float depth = start;

    for(int i = 0; i < maxSteps; i++) {

        stepRatio = i;

        float dist = sceneSDF(eye + depth * direction, colour);

        if(dist < epsilon)
            return depth;

        depth += dist;
        if(depth >= end)
            return end;
    }

    return end;
}

vec3 EstimateNormal(vec3 p, float epsilon) {
    vec4 dummyCol = vec4(1.0);
    return normalize(vec3(
            sceneSDF(vec3(p.x + epsilon, p.y, p.z), dummyCol)  - sceneSDF(vec3(p.x - epsilon, p.y, p.z), dummyCol),
            sceneSDF(vec3(p.x, p.y + epsilon, p.z), dummyCol)  - sceneSDF(vec3(p.x, p.y - epsilon, p.z), dummyCol),
            sceneSDF(vec3(p.x, p.y, p.z  + epsilon), dummyCol) - sceneSDF(vec3(p.x, p.y, p.z - epsilon), dummyCol)
    ));
}

vec3 phongLightingContrib(vec3 diffuse, vec3 specular, float shininess, vec3 pos, vec3 eye, vec3 lightPos, vec3 intensity) {
    vec3 N = EstimateNormal(pos, epsilon);
    vec3 L = normalize(lightPos - pos);
    vec3 V = normalize(eye - pos);
    vec3 R = normalize(reflect(-L, N));

    float LdotN = dot(L, N);
    float RdotV = dot(R, V);

    // Light not visible from point on surface
    if(LdotN < 0.0)
        return vec3(0.0);

    // Light reflection in opposite direction
    if(RdotV < 0.0)
        return intensity * (diffuse * LdotN);

    return intensity * (diffuse * LdotN + specular * pow(RdotV, shininess));
}

vec3 PhongIllumination(vec3 ambient, vec3 diffuse, vec3 specular, float alpha, vec3 pos, vec3 eye) {
    const vec3 ambientLight = vec3(0.5);
    vec3 colour = ambientLight * ambient;

    vec3 light1Pos = vec3(4.0 * sin(time), 2.0, 4.0 * cos(time));
    vec3 light1Intensity = vec3(0.4);

    colour += phongLightingContrib(diffuse, specular, alpha, pos, eye, light1Pos, light1Intensity);

    vec3 light2Pos = vec3(2.0 * sin(0.37 * time), 2.0 * cos(0.37 * time), 2.0);
    vec3 light2Intensity = vec3(0.4);
    colour += phongLightingContrib(diffuse, specular, alpha, pos, eye, light2Pos, light2Intensity);
    return colour;
}

vec3 rayDirection(float fieldOfView, vec2 size, vec2 fragCoord) {
    vec2 xy = fragCoord - size / 2.0;
    float z = size.y / tan(radians(fieldOfView) / 2.0);
    return normalize(vec3(xy, -z));
}

void main() {

    float steps = 0.0;

    vec3 dir = normalize(rayDirection(fov, screenSize, Position.xy));
    dir = (vec4(dir, 0.0) * view).xyz;

    vec4 col = vec4(1.0);

    float d = GetDepth(position, dir, rayStart, rayEnd, steps, col);

    if(d > rayEnd - epsilon) {
        FinalColour = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }


    vec3 p = position + d * dir;
    vec3 N = EstimateNormal(p, epsilon);
    vec3 ambient = vec3(0.2);
    vec3 diffuse = col.rgb;
    vec3 specular = vec3(1.0);
    float shininess = 10.0;

    vec3 colour = PhongIllumination(ambient, diffuse, specular, shininess, p, position);

	FinalColour = vec4(colour, 1.0);
}