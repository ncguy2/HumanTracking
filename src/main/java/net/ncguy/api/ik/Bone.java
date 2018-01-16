package net.ncguy.api.ik;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.utils.MathsUtils;

public class Bone {

    protected transient BoneNode structure;

    public boolean propagateToChildren = true;

    public boolean hasParent = false;

    protected float length;

    public final Vector3 target = new Vector3();

    protected Vector3 position = new Vector3();
    public final Vector3 inertia = new Vector3();
    protected Vector3 direction = new Vector3();


    public transient final Vector3 forward = new Vector3();

    public String name;

    public float Length() {
        return length;
    }

    public void setStructure(BoneNode node) {
        this.structure = node;
    }

    public Bone(String name) {
        this(name, 0, 0, 0, 0);
    }

    public Bone(String name, float x, float y, float z, float length) {
        this.name = name;
        this.position.set(x, y, z);
        this.target.set(this.position);
        this.direction.set(0, -1, 0).nor();
        this.length = length;
        this.hasParent = false;
    }

    public Bone(String name, Vector3 pos, float length) {
        this(name, pos.x, pos.y, pos.z, length);
    }

    public Bone(String name, float x, float y, float length) {
        this(name, x, y, 0, length);
    }

    public Bone(String name, Bone parent) {
        this.name = name;
        this.position.set(parent.EndPosition());
        this.direction.set(MathsUtils.GetDirection(this.position, parent.position));
        this.length = this.position.cpy().sub(parent.position).len();
        this.hasParent = parent != null;
    }

    public void InvalidateForward(float distance) {
        forward.set(position).add(direction.cpy().scl(distance));
    }

    public Vector3 EndPosition() {
        return Extend(length);
    }

    public Vector3 Extend(float distance) {
        return position.cpy().add(direction.cpy().scl(distance));
    }

    public Vector3 GetTarget() {
        return target;
    }

    public Vector3 GetPosition() {
        return position;
    }

    public void SetPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public void SetPosition(Vector3 vec) {
        SetPosition(vec.x, vec.y, vec.z);
    }

    public Vector3 GetDirection() {
        return direction;
    }

    public void SetDirection(Vector3 vec) {
        SetDirection(vec.x, vec.y, vec.z);
    }

    public void Update() {}

    public void SetDirection(float x, float y, float z) {
        direction.set(x, y, z);
    }

    @Override
    public String toString() {
        return name;
    }

    public void SetTarget(Vector3 newTarget) {
        this.target.set(newTarget);
    }

    public void SetScale(float x, float y, float z) {

    }

    public Vector3 TransformHomePosition(Vector3 pos) {
        return pos.cpy();
    }

    public Vector3 TransformHomeDirection(Vector3 dir) {
        return dir.cpy();
    }

    public Vector3 InverseTransformHomePosition(Vector3 pos) {
        return pos.cpy();
    }

    public Vector3 InverseTransformHomeDirection(Vector3 dir) {
        return dir.cpy();
    }

    public Vector3 Transform(Vector3 vec) {
        return vec.cpy();
    }

    public Vector3 InvTransform(Vector3 vec) {
        return vec.cpy();
    }

    public Vector3 GetModelPosition() {
        return position;
    }

    public void SetModelPosition(Vector3 pos) {
        position.set(pos);
    }


    public void GetWorldTransform(Matrix4 worldTrans) {

    }

    public void SetWorldTransform(Matrix4 worldTrans) {
    }

}
