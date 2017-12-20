package net.ncguy.tracking.geometry;

public abstract class BaseGeometry {

    public abstract GeometryTypes GetId();
    public abstract Vec4 GetVec4A();
    public abstract Vec4 GetVec4B();
    public abstract float GetFloatVar();

    @Override
    public String toString() {
        return GetId().name();
    }

    public static class Vec4 {
        public float x;
        public float y;
        public float z;
        public float w;

        public Vec4() {
            this(0, 0, 0, 0);
        }

        public Vec4(float x) {
            this(x, x, x, x);
        }

        public Vec4(float x, float y, float z, float w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        public float[] Values() {
            return new float[] { x, y, z, w };
        }

    }

}
