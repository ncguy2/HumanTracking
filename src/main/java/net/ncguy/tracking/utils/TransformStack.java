package net.ncguy.tracking.utils;

import com.badlogic.gdx.math.Matrix4;

import java.util.Stack;

public class TransformStack extends Stack<Matrix4> {

    private Matrix4 transform;

    public TransformStack() {
        transform = new Matrix4();
    }

    public Matrix4 Transform() {
        return transform;
    }

    @Override
    public Matrix4 push(Matrix4 item) {
        this.transform.mul(item);
        return super.push(item);
    }

    @Override
    public synchronized Matrix4 pop() {
        Matrix4 pop = super.pop();

        this.transform.mul(pop.cpy().inv());

        return pop;
    }
}
