package net.ncguy.skeleton;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SKJoint {

    protected Vector3 position = new Vector3();
    protected Vector3 direction = new Vector3();
    protected Vector3 target = new Vector3();

    protected Vector3 inertia = new Vector3();

    protected final Vector3 homePosition = new Vector3();
    protected final Vector3 homeDirection = new Vector3();

    public SKBone parentBone;
    public List<SKBone> childrenBones;

    public String id = UUID.randomUUID().toString();

    public SKJoint(SKBone parentBone) {
        this.parentBone = parentBone;
        this.childrenBones = new ArrayList<>();
        PropagateToParentBone();
    }

    public void PropagateToParentBone() {
        if(parentBone != null)
            this.parentBone.SetEnd(this);
    }

    public void Attach(SKJoint joint) {
        Attach(new SKBone(this, joint));
    }

    public void DetachFromParent() {
        parentBone = null;
    }

    public void AttachToParent(SKBone bone) {
        parentBone = bone;
    }


    public void Attach(SKBone bone) {
        childrenBones.add(bone);
    }

    public boolean IsOrphan() {
        return parentBone == null || GetConnectedJoint(parentBone) == null;
    }

    public boolean IsLeaf() {
        return childrenBones.isEmpty();
    }

    public SKJoint GetParentJoint() {
        if(IsOrphan())
            return null;
        return GetConnectedJoint(parentBone);
    }

    public SKJoint GetConnectedJoint(SKBone bone) {
        return bone.Other(this);
    }

    public SKJoint GetChildJoint(int idx) {

        if(idx < 0 || idx >= childrenBones.size())
            return null;

        SKBone skBone = childrenBones.get(idx);
        if(skBone != null)
            GetConnectedJoint(skBone);

        return null;
    }

    public Vector3 GetDirectionOfBone(SKBone bone) {
        SKJoint skJoint = GetConnectedJoint(bone);
        return skJoint.position
                .cpy()
                .sub(this.position)
                .nor();
    }

    public Vector3 GetDirection() {
        return direction;
    }

    public void SetDirection(Vector3 dir) {
        this.direction.set(dir);
    }

    public Vector3 GetPosition() {
        return position;
    }

    public Vector3 GetTarget() {
        return target;
    }

    public void SetPosition(Vector3 vec) {
        position.set(vec);
    }

    public void SetPositionMem(Vector3 vec) {
        position = vec;
        SetHomePosition(vec);
    }

    public void SetDirectionMem(Vector3 vec) {
        direction = vec;
        SetHomeDirection(vec);
    }

    public void SetTarget(Vector3 vec) {
        target.set(vec);
    }

    public void SetTargetMem(Vector3 vec) {
        target = vec;
    }

    public SKJoint FindRoot() {
        if(IsOrphan())
            return this;
        return GetParentJoint().FindRoot();
    }

    public List<SKJoint> GetChildJoints() {
        return childrenBones.stream()
                .map(b -> b.Other(this))
                .collect(Collectors.toList());
    }

    public SKJoint Find(String id) {
        return Find(id, true);
    }
    public SKJoint Find(String id, boolean strict) {

        if(strict) {
            if (this.id.equalsIgnoreCase(id))
                return this;
        }else {
            if (this.id.toLowerCase().contains(id.toLowerCase()))
                return this;
        }

        for (SKJoint child : GetChildJoints()) {
            SKJoint found = child.Find(id, strict);
            if(found != null) return found;
        }

        return null;
    }

    public String Print() {
        StringBuilder sb = new StringBuilder();
        Print(sb, "");
        return sb.toString();
    }

    public void Print(StringBuilder sb, String prefix) {
        sb.append(prefix).append(id).append(": ").append(GetPosition()).append("\n");

        childrenBones.forEach(bone -> {
            sb.append(prefix).append("BONE ").append(bone.length).append("\n");
            bone.Other(this).Print(sb, prefix + "  ");
        });
    }

    public void SetScale(float uniformScale) {
        SetScale(uniformScale, uniformScale, uniformScale);
    }

    public void SetScale(float x, float y, float z) {

    }

    public void SetHomePosition(Vector3 pos) {
        this.homePosition.set(pos);
    }

    public void SetHomeDirection(Vector3 dir) {
        this.homeDirection.set(dir);
    }

    public Vector3 GetHomePosition() {
        return this.homePosition;
    }

    public Vector3 GetHomeDirection() {
        return this.homeDirection;
    }

    public void Reset(boolean propagate) {
        ResetDirection(propagate);
        ResetPosition(propagate);
    }

    public void ResetPosition(boolean propagate) {
        SetPosition(this.homePosition);

        if(propagate) {
            childrenBones.stream()
                    .filter(SKBone::IsValid)
                    .map(b -> b.Other(SKJoint.this))
                    .forEach(j -> j.ResetPosition(propagate));
        }
    }

    public void ResetDirection(boolean propagate) {
        SetDirection(this.homeDirection);

        if(propagate) {
            childrenBones.stream()
                    .filter(SKBone::IsValid)
                    .map(b -> b.Other(SKJoint.this))
                    .forEach(j -> j.ResetDirection(propagate));
        }
    }

    public Vector3 Transform(Vector3 vec) {
        return vec.cpy();
    }

    public Vector3 InvTransform(Vector3 vec) {
        return vec.cpy();
    }

    public Vector3 GetInertia() {
        return inertia;
    }

    public void SetInertia(Vector3 vec) {
        this.inertia.set(vec);
    }

    public void SetInertiaMem(Vector3 vec) {
        this.inertia = vec;
    }

    public float GetLength() {
        if(IsOrphan())
            return 0;
        return parentBone.length;
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
