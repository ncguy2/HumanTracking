package net.ncguy.utils;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class MathsUtils {

    public static float Lerp(float a, float b, float alpha) {
        return a + (b - a) * alpha;
    }

    public static Vector2 Lerp(Vector2 a, Vector2 b, float alpha) {
        Vector2 res = new Vector2();
        res.x = Lerp(a.x, b.x, alpha);
        res.y = Lerp(a.y, b.y, alpha);
        return res;
    }

    public static Vector3 Lerp(Vector3 a, Vector3 b, float alpha) {
        Vector3 res = new Vector3();
        res.x = Lerp(a.x, b.x, alpha);
        res.y = Lerp(a.y, b.y, alpha);
        res.z = Lerp(a.z, b.z, alpha);
        return res;
    }

    public static void Div(Vector3 vec, float scalar) {
        if(scalar == 0)
            throw new IllegalStateException("scalar cannot be 0, would result in a DivideByZero exception");
        vec.x /= scalar;
        vec.y /= scalar;
        vec.z /= scalar;
    }

    public static boolean ApproximatelyEquals(float a, float b, float tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    public static boolean ApproximatelyEquals(Vector3 a, Vector3 b, float tolerance) {
        return  ApproximatelyEquals(a.x, b.x, tolerance) &&
                ApproximatelyEquals(a.y, b.y, tolerance) &&
                ApproximatelyEquals(a.z, b.z, tolerance);
    }

    public static Vector3 Sub(Vector3 a, Vector3 b) {
        return a.cpy().sub(b);
    }

    public static Vector3 Forward(Vector3 pos, Vector3 dir, float dist) {
        return pos.cpy().add(dir.cpy().scl(dist));
    }

    public static Quaternion RotateFromTo(Vector3 from, Vector3 to) {

        from = from.cpy().nor();
        to = to.cpy().nor();


        if(from.equals(to.cpy().scl(-1)))
            return new Quaternion(to, 0);

        Vector3 half = from.cpy().add(to).nor();

        return new Quaternion(to.cpy().crs(half), to.cpy().dot(half));

//        Vector3 axis = from.cpy().crs(to);
//
//        float dot = from.cpy().dot(to);
//        float s = (float) (Math.sqrt((from.len() * from.len()) * (to.len() * to.len())) + dot);
//        float s = (float) Math.sqrt((1 + dot) * 2);
//        float invs = 1 / s;
//        dot /= from.len() * to.len();
//        float angle = (float) Math.cos(dot);

//        return new Quaternion(axis, s).nor();
    }

    public static Vector3 GetForwardVector(Quaternion q) {

//        Vector3 res = new Vector3(Vector3.X);
//        q.transform(res);
//        return res;

//        return new Vector3(q.x, q.y, q.z);

        // Up
//        return new Vector3(
//                2 * (q.x * q.y - q.w * q.z),
//                1 - 2 * (q.w * q.w + q.z * q.z),
//                2 * (q.y * q.z + q.w * q.x));

        // Right
//        return new Vector3(
//                1 - 2 * (q.y * q.y + q.z * q.z),
//                2 * (q.x * q.y + q.w * q.z),
//                2 * (q.x * q.z - q.w * q.y));

        // Forward
        return new Vector3(
                2 * (q.x * q.z + q.w * q.y),
                2 * (q.y * q.x - q.w * q.z),
                1 - 2 * (q.x * q.x + q.y * q.y));
    }

    public static Vector3 GetDirection(Vector3 from, Vector3 to) {
        return to.cpy().sub(from).nor();
    }

    public static Vector3 PerpendicularVector(Vector3 vec) {
        Vector3 p = new Vector3();

        if(Math.abs(vec.y) < .99f)
            p.set(-vec.z, 0.f, vec.x);
        else p.set(0.f, vec.z, -vec.y);

        return p.nor();
    }

    public static float GetAngleBetweenRads(Vector3 a, Vector3 b) {
        return (float) Math.acos(a.dot(b));
    }

    public static float GetAngleBetweenDegs(Vector3 a, Vector3 b) {
        return (float) Math.toDegrees(GetAngleBetweenRads(a, b));
    }

    public static Vector3 RotateAboutAxisRads(Vector3 src, float angle, Vector3 axis) {
        Quaternion q = new Quaternion(axis, angle);
        return src.cpy().mul(q);
    }

    public static Vector3 RotateAboutAxisDegs(Vector3 src, float angle, Vector3 axis) {
        return RotateAboutAxisRads(src, (float) Math.toRadians(angle), axis);
    }

    public static Vector3 GetAngleLimitedUnitVectorDegs(Vector3 limit, Vector3 baseline, float angleLimit) {
        float angle = GetAngleBetweenDegs(baseline, limit);

        if(angle > angleLimit) {
            Vector3 correction = baseline.cpy().nor().crs(limit.cpy().nor()).nor();
            return RotateAboutAxisDegs(baseline, angleLimit, correction).nor();
        }

        return limit.cpy().nor();

    }

    public static Vector3 ProjectOntoPlane(Vector3 vec, Vector3 normal) {
        if ( !(normal.len() > 0.0f) ) { throw new IllegalArgumentException("Plane normal cannot be a zero vector."); }
        Vector3 b = vec.cpy().nor();
        Vector3 n = normal.cpy().nor();
        return b.sub(n.scl(b.dot(normal))).nor();
    }

    public static Matrix3 CreateRotationMatrix(Vector3 referenceDirection)
    {
        Vector3 xAxis;
        Vector3 yAxis;
        Vector3 zAxis = referenceDirection.cpy().nor();

        // Handle the singularity (i.e. bone pointing along negative Z-Axis)...
        if(referenceDirection.z < -0.9999999f)
        {
            xAxis = new Vector3(1.0f, 0.0f, 0.0f); // ...in which case positive X runs directly to the right...
            yAxis = new Vector3(0.0f, 1.0f, 0.0f); // ...and positive Y runs directly upwards.
        }
        else
        {
            float a = 1.0f/(1.0f + zAxis.z);
            float b = -zAxis.x * zAxis.y * a;
            xAxis = new Vector3(1.0f - zAxis.x * zAxis.x * a, b, -zAxis.x).nor();
            yAxis = new Vector3(b, 1.0f - zAxis.y * zAxis.y * a, -zAxis.y).nor();
        }

        Matrix3 m = new Matrix3();

        m.val[Matrix3.M00] = xAxis.x;
        m.val[Matrix3.M01] = xAxis.y;
        m.val[Matrix3.M02] = xAxis.z;

        m.val[Matrix3.M10] = yAxis.x;
        m.val[Matrix3.M11] = yAxis.y;
        m.val[Matrix3.M12] = yAxis.z;

        m.val[Matrix3.M20] = zAxis.x;
        m.val[Matrix3.M21] = zAxis.y;
        m.val[Matrix3.M22] = zAxis.z;

        return m;
    }
    
    public static Vector3 Mul(Matrix3 mat, Vector3 src) {
        return new Vector3(mat.val[Matrix3.M00] * src.x + mat.val[Matrix3.M10] * src.y + mat.val[Matrix3.M20] * src.z,
                mat.val[Matrix3.M01] * src.x + mat.val[Matrix3.M11] * src.y + mat.val[Matrix3.M21] * src.z,
                mat.val[Matrix3.M02] * src.x + mat.val[Matrix3.M12] * src.y + mat.val[Matrix3.M22] * src.z);
    }

    public static Quaternion ToQuaternion(Vector3 dir) {

        Vector3 forward = dir.cpy().nor();
        Vector3 right = Vector3.Y.cpy().crs(forward);
        Vector3 up = forward.cpy().crs(right);

        Matrix3 m = new Matrix3();

        m.val[Matrix3.M00] = forward.x;
        m.val[Matrix3.M01] = forward.y;
        m.val[Matrix3.M02] = forward.z;

        m.val[Matrix3.M10] = right.x;
        m.val[Matrix3.M11] = right.y;
        m.val[Matrix3.M12] = right.z;

        m.val[Matrix3.M20] = up.x;
        m.val[Matrix3.M21] = up.y;
        m.val[Matrix3.M22] = up.z;

        return new Quaternion().setFromMatrix(m);
    }

    public static byte[] IntToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
}
