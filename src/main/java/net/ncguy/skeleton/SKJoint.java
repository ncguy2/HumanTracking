package net.ncguy.skeleton;

import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SKJoint {

    protected Vector3 position = new Vector3();
    protected Vector3 direction = new Vector3();
    protected Vector3 target = new Vector3();

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
    }

    public void SetDirectionMem(Vector3 vec) {
        direction = vec;
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

        if(this.id.equalsIgnoreCase(id))
            return this;

        for (SKJoint child : GetChildJoints()) {
            SKJoint found = child.Find(id);
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

}
