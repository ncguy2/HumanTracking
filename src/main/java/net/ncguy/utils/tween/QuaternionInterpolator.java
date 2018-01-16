package net.ncguy.utils.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.math.Quaternion;

import java.util.function.Supplier;

public class QuaternionInterpolator {

    public Quaternion start;
    public Quaternion target;
    private final Supplier<Quaternion> supplier;
    private final Runnable callback;
    public float alpha;

    public QuaternionInterpolator(Quaternion start, Quaternion target, Supplier<Quaternion> supplier) {
        this(start, target, supplier, null);
    }

    public QuaternionInterpolator(Quaternion start, Quaternion target, Supplier<Quaternion> supplier, Runnable callback) {
        this.start = new Quaternion(start);
        this.target = target;
        this.supplier = supplier;
        this.callback = callback;
        this.alpha = 0;
    }

    public void Apply(float alpha) {
        this.alpha = alpha;
        Quaternion q = supplier.get();
        q.set(start);
        q.slerp(target, alpha);
        if(callback != null)
            callback.run();
    }

    public static class QITweenAccessor implements TweenAccessor<QuaternionInterpolator> {

        @Override
        public int getValues(QuaternionInterpolator q, int i, float[] floats) {
            floats[0] = q.alpha;
            return 1;
        }

        @Override
        public void setValues(QuaternionInterpolator q, int i, float[] floats) {
            q.Apply(floats[0]);
        }

    }
}
