package net.ncguy.physics.sim;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.badlogic.gdx.utils.Disposable;
import net.ncguy.skeleton.SKJoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class PhyJoint implements Disposable {

    private static Vector3 localInertia = new Vector3();

    public transient final SKJoint joint;
    public PhysicsType type;
    public JointMotionState motionState;
    public btRigidBody body;

    public float mass;
    public btCollisionShape shape;

    public List<btTypedConstraint> constraints;

    public PhyJoint(SKJoint joint, PhysicsType type) {
        this.joint = joint;
        this.type = type;
        this.constraints = new ArrayList<>();

        mass = type == PhysicsType.STATIC ? 0.f : 1.f;

//        Shape(new btSphereShape(.1f));
        Shape(new btCapsuleShape(.1f, joint.GetLength()));
    }

    public PhyJoint Add(btTypedConstraint constraint) {
        this.constraints.add(constraint);
        return this;
    }

    public void Body(btRigidBody body) {
        this.body = body;
    }

    public void Shape(btCollisionShape shape) {

        if(this.shape != null) {
            this.shape.dispose();
            this.shape = null;
        }

        this.shape = shape;
    }

    public boolean Build(PhyWorld world) {
        if(body != null) return true;
        if(shape == null) return false;

        PhyJoint parent = world.Get(joint.GetParentJoint());

        if(parent == null) {
            Shape(new btSphereShape(.1f));
        }

        motionState = new JointMotionState(this.joint);

        if(mass > 0f)
            shape.calculateLocalInertia(mass, localInertia);
        else localInertia.set(0, 0, 0);

        btRigidBody.btRigidBodyConstructionInfo ctorInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);

        body = new btRigidBody(ctorInfo);
        body.setMotionState(motionState);

        // TODO constraints

        float halfHeight = joint.GetLength() * .5f;

        if(parent != null) {
            if(parent.Build(world)) {
                btPoint2PointConstraint constraint = new btPoint2PointConstraint(parent.body, this.body, new Vector3(0, halfHeight, 0), new Vector3(0, -halfHeight, 0));
                constraints.add(constraint);
            }
        }else{
            btPoint2PointConstraint constraint = new btPoint2PointConstraint(this.body, new Vector3(0, -.05f, 0));
            constraints.add(constraint);
        }


        ctorInfo.dispose();
        ctorInfo = null;

        return body != null;
    }

    public void Constraints(Consumer<btTypedConstraint> func) {
        constraints.forEach(func);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhyJoint phyJoint = (PhyJoint) o;
        return Objects.equals(joint, phyJoint.joint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(joint);
    }

    @Override
    public void dispose() {

        if(shape != null) {
            shape.dispose();
            shape = null;
        }

        if(body != null) {
            body.dispose();
            body = null;
        }
    }
}
