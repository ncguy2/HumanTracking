#version 330

#define MAX_MODELS 16
#define MAX_TREE_NODES 256

#define OPERATION_NONE 0
#define OPERATION_UNION 1
#define OPERATION_INTERSECTION 2
#define OPERATION_DIFFERENCE 3

#pragma include("includes/SDFGeometry.glsl")

//uniform GeometryItem rootItem;
//uniform GeometryItem items[MAX_TREE_NODES];
uniform Geometry models[MAX_MODELS];

//#pragma include("includes/TreeTraversal.glsl")

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

float StaticSceneSDF(vec3 samplePoint, out vec4 colour);

float SumValues(float curVal, float next, int operationId, out float percent) {
    switch(operationId) {
        case OPERATION_UNION:
            return UnionSDF(curVal, next, percent);
        case OPERATION_INTERSECTION:
            return IntersectSDF(curVal, next, percent);
        case OPERATION_DIFFERENCE:
            return DifferenceSDF(curVal, next, percent);
    }

    return curVal;
}

float sceneSDF(vec3 samplePoint, out vec4 colour) {
    float value = 0;

    for(int i = 0; i < MAX_MODELS; i++) {
        Geometry g = models[i];
        if(!g.valid)
            return value;

        float v = SDF(samplePoint, g);
        float percent;
        value = SumValues(value, v, g.operation, percent);
        percent = clamp(percent, 0.0, 1.0);
        colour = mix(colour, g.colour, 1 - percent);
    }

    return value;
}

/**
 * Rotation matrix around the X axis.
 */
mat3 rotateX(float theta) {
    float c = cos(theta);
    float s = sin(theta);
    return mat3(
        vec3(1, 0, 0),
        vec3(0, c, -s),
        vec3(0, s, c)
    );
}

/**
 * Rotation matrix around the Y axis.
 */
mat3 rotateY(float theta) {
    float c = cos(theta);
    float s = sin(theta);
    return mat3(
        vec3(c, 0, s),
        vec3(0, 1, 0),
        vec3(-s, 0, c)
    );
}

/**
 * Rotation matrix around the Z axis.
 */
mat3 rotateZ(float theta) {
    float c = cos(theta);
    float s = sin(theta);
    return mat3(
        vec3(c, -s, 0),
        vec3(s, c, 0),
        vec3(0, 0, 1)
    );
}

float StaticSceneSDF(vec3 samplePoint, out vec4 colour) {

    samplePoint = rotateY(time / 2.0) * samplePoint;

    float percent;

    float cylinderRadius = 0.4 + (1.0 - 0.4) * (1.0 + sin(1.7 * time)) / 2.0;
    float cylinder1 = CylinderCapped(samplePoint, vec2(2.0, cylinderRadius));
    float cylinder2 = CylinderCapped(rotateX(radians(90.0)) * samplePoint, vec2(2.0, cylinderRadius));
    float cylinder3 = CylinderCapped(rotateY(radians(90.0)) * samplePoint, vec2(2.0, cylinderRadius));

    float cube = Box(samplePoint, vec3(1.8, 1.8, 1.8));

    float sphere = Sphere(samplePoint, 1.2);

    float ballOffset = 0.4 + 1.0 + sin(1.7 * time);
    float ballRadius = 0.3;
    float balls = Sphere(samplePoint - vec3(ballOffset, 0.0, 0.0), ballRadius);
    balls = UnionSDF(balls, Sphere(samplePoint + vec3(ballOffset, 0.0, 0.0), ballRadius), percent);
    balls = UnionSDF(balls, Sphere(samplePoint - vec3(0.0, ballOffset, 0.0), ballRadius), percent);
    balls = UnionSDF(balls, Sphere(samplePoint + vec3(0.0, ballOffset, 0.0), ballRadius), percent);
    balls = UnionSDF(balls, Sphere(samplePoint - vec3(0.0, 0.0, ballOffset), ballRadius), percent);
    balls = UnionSDF(balls, Sphere(samplePoint + vec3(0.0, 0.0, ballOffset), ballRadius), percent);



    float csgNut = DifferenceSDF(IntersectSDF(cube, sphere, percent),
                         UnionSDF(cylinder1, UnionSDF(cylinder2, cylinder3, percent), percent), percent);

    return UnionSDF(balls, csgNut, percent);
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

float Shadow(vec3 ro, vec3 rd, float minVal, float maxVal, float k) {
    vec4 dummy = vec4(1.0);
    float res = 1.0;
    for(float t = minVal; t < maxVal; ) {
        float h = sceneSDF(ro + rd * t, dummy);
        if(h < epsilon)
            return 0.0;
        res = min(res, k * h / t);
        t += h;
    }
    return res;
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

    float shadow = Shadow(lightPos, L, epsilon, length(lightPos - pos), 2);
    diffuse *= shadow;

    vec3 c = intensity * (diffuse * LdotN + specular * pow(RdotV, shininess));

    return c;
}

vec3 PhongIllumination(vec3 ambient, vec3 diffuse, vec3 specular, float alpha, vec3 pos, vec3 eye) {
    const vec3 ambientLight = vec3(0.5);
    vec3 colour = ambientLight * ambient;

    vec3 light1Pos = vec3(4.0 * sin(time), 2.0, 4.0 * cos(time));
    vec3 light1Intensity = vec3(0.4);

    colour += phongLightingContrib(diffuse, specular, alpha, pos, eye, light1Pos, light1Intensity);

//    vec3 light2Pos = vec3(2.0 * sin(0.37 * time), 2.0 * cos(0.37 * time), 2.0);
//    vec3 light2Intensity = vec3(0.4);
//    colour += phongLightingContrib(diffuse, specular, alpha, pos, eye, light2Pos, light2Intensity);
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