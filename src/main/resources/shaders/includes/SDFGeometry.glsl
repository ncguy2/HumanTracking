#define GEOM_SPHERE 0
#define GEOM_BOX 1
#define GEOM_TORUS 2
#define GEOM_CYLINDER 3
#define GEOM_CONE 4
#define GEOM_PLANE 5
#define GEOM_PRISM_HEX 6
#define GEOM_PRISM_TRI 7
#define GEOM_CAPSULE 8
#define GEOM_CYLINDER_CAPPED 9
#define GEOM_CONE_CAPPED 10
#define GEOM_ELLIPSOID 11

float smin_exp(float a, float b, float k) {
    float res = exp(-k * a) + exp(-k * b);
    return -log(res) / k;
}

float smin_poly(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

float smin_pow(float a, float b, float k) {
    a = pow(a, k);
    b = pow(b, k);
    return pow((a * b) / (a + b), 1.0 / k);
}

float CalculatePercentBetween(float A, float B, float M) {
    float tmpMin = min(A, B);
    float tmpMax = max(A, B);

    tmpMax -= tmpMin;
    float tmpM = M - tmpMin;

    return tmpM / tmpMax;
}

float IntersectSDF(float distA, float distB, out float percent) {
    float m = max(distA, distB);
    percent = CalculatePercentBetween(distA, distB, m);
    return m;
}

float UnionSDF(float distA, float distB, out float percent) {
    float m = smin_poly(distA, distB, .5);
    percent = CalculatePercentBetween(distA, distB, m);
    return m;
}

float DifferenceSDF(float distA, float distB, out float percent) {
    float m = max(distA, -distB);
    percent = CalculatePercentBetween(distA, -distB, m);
    return m;
}

struct Geometry {
    int id;
    mat4 transform;
    vec4 vec4A;
    vec4 vec4B;
    float floatVar;
    bool valid;
    vec4 colour;
    int operation;
};

struct GeometryItem {
    int leftItem;
    int rightItem;
    int data;
    int operation;

    bool valid;

    float value;
    bool calculated;
};

vec3 TransformSamplePoint(vec3 samplePoint, mat4 transform) {
    return (transform * vec4(samplePoint, 1.0)).xyz;
}

float Sphere(vec3 sp, float rad);
float Box(vec3 sp, vec3 size);
float Torus(vec3 sp, vec2 rads);
float Cylinder(vec3 sp, vec3 size);
float Cone(vec3 sp, vec2 rads);
float Plane(vec3 sp, vec4 size);
float HexPrism(vec3 sp, vec2 size);
float TriPrism(vec3 sp, vec2 size);
float Capsule(vec3 sp, vec3 a, vec3 b, float rad);
float CylinderCapped(vec3 sp, vec2 size);
float ConeCapped(vec3 sp, vec3 size);
float Ellipsoid(vec3 sp, vec3 rads);

float SDF(vec3 samplePoint, Geometry geom) {
    vec3 point = TransformSamplePoint(samplePoint, geom.transform);
//    vec3 point = samplePoint;

    switch(geom.id) {
        case GEOM_SPHERE:
            return Sphere(point, geom.floatVar);
        case GEOM_BOX:
            return Box(point, geom.vec4A.xyz);
        case GEOM_TORUS:
            return Torus(point, geom.vec4A.xy);
        case GEOM_CYLINDER:
            return Cylinder(point, geom.vec4A.xyz);
        case GEOM_CONE:
            return Cone(point, geom.vec4A.xy);
        case GEOM_PLANE:
            return Plane(point, geom.vec4A);
        case GEOM_PRISM_HEX:
            return HexPrism(point, geom.vec4A.xy);
        case GEOM_PRISM_TRI:
            return TriPrism(point, geom.vec4A.xy);
        case GEOM_CAPSULE:
            return Capsule(point, geom.vec4A.xyz, geom.vec4B.xyz, geom.floatVar);
        case GEOM_CYLINDER_CAPPED:
            return CylinderCapped(point, geom.vec4A.xy);
        case GEOM_CONE_CAPPED:
            return ConeCapped(point, geom.vec4A.xyz);
        case GEOM_ELLIPSOID:
            return Ellipsoid(point, geom.vec4A.xyz);
    }

    return -1.0;
}

float Sphere(vec3 sp, float rad) {
    return length(sp) - rad;
}

float Box(vec3 sp, vec3 size) {
    vec3 d = abs(sp) - size;
    float insideDistance = min(max(d.x, max(d.y, d.z)), 0.0);
    float outsideDistance = length(max(d, 0.0));
    return insideDistance + outsideDistance;
}

float Torus(vec3 sp, vec2 rads) {
    vec2 q = vec2(length(sp.xz) - rads.x, sp.y);
    return length(q) - rads.y;
}

float Cylinder(vec3 sp, vec3 size) {
    return length(sp.xz - size.xy) - size.z;
}

float Cone(vec3 sp, vec2 rads) {
    rads = normalize(rads);
    float q = length(sp.xy);
    return dot(rads, vec2(q, sp.z));
}

float Plane(vec3 sp, vec4 size) {
    size = normalize(size);
    return dot(sp, size.xyz) + size.w;
}

float HexPrism(vec3 sp, vec2 size) {
    vec3 q = abs(sp);
    return max(q.z - size.y, max(q.x*0.866025 + q.y*0.5, q.y) - size.x);
}

float TriPrism(vec3 sp, vec2 size) {
    vec3 q = abs(sp);
    return max(q.z - size.y, max(q.x*0.866025 + q.y*0.5, -sp.y) - size.x*0.5);
}

float Capsule(vec3 sp, vec3 a, vec3 b, float rad) {
    vec3 pa = sp - a;
    vec3 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba*h) - rad;
}

float CylinderCapped(vec3 sp, vec2 size) {
    vec2 d = abs(vec2(length(sp.xz), sp.y)) - size;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0));
}

float ConeCapped(vec3 sp, vec3 size) {
    vec2 q = vec2(length(sp.xz), sp.y);
    vec2 v = vec2(size.z * size.y / size.x, -size.z);
    vec2 w = v - q;
    vec2 vv = vec2(dot(v, v), v.x * v.x);
    vec2 qv = vec2(dot(v, w), v.x * w.x);
    vec2 d = max(qv, 0.0) * qv / vv;
    return sqrt(dot(w, w) - max(d.x, d.y)) * sign(max(q.y*v.x - q.x*v.y, w.y));
}

float Ellipsoid(vec3 sp, vec3 rads) {
    return (length(sp / rads) - 1.0) * min(min(rads.x, rads.y), rads.z);
}

