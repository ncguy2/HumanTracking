package net.ncguy.tracking.geometry;

public class DefaultGeometry extends BaseGeometry {

    GeometryTypes id;
    Vec4 a;
    Vec4 b;
    float val;

    public DefaultGeometry(GeometryTypes id, Vec4 a, Vec4 b, float val) {
        this.id = id;
        this.a = a;
        this.b = b;
        this.val = val;
    }

    @Override
    public GeometryTypes GetId() {
        return id;
    }

    @Override
    public Vec4 GetVec4A() {
        return a;
    }

    @Override
    public Vec4 GetVec4B() {
        return b;
    }

    @Override
    public float GetFloatVar() {
        return val;
    }
}
