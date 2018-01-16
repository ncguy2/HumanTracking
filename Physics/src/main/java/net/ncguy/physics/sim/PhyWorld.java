package net.ncguy.physics.sim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Disposable;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ListChangeListener;
import net.ncguy.skeleton.SKJoint;

import java.util.ArrayList;
import java.util.List;

public class PhyWorld implements Disposable {

    public DebugDrawer debugDrawer = null;

    public btCollisionConfiguration collisionConfiguration;
    public btCollisionDispatcher dispatcher;
    public btBroadphaseInterface broadphase;
    public btConstraintSolver solver;
    public btDynamicsWorld collisionWorld;

    public Vector3 defaultGravity;
    ObservableListWrapper<PhyJoint> simulatedJoints;

    public int maxSubSteps = 5;
    public float fixedTimeStep = 1f / 60f;

    public PhyWorld() {
        simulatedJoints = new ObservableListWrapper<>(new ArrayList<>());
        defaultGravity = new Vector3(0, -9.81f, 0);
        InitializeWorld();
        AttachListeners();
    }

    public void InitializeWorld() {
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);

        collisionWorld.setGravity(defaultGravity);
    }

    public void AttachListeners() {
        simulatedJoints.addListener((ListChangeListener<? super PhyJoint>) c -> {
            while(c.next()) {
                if (c.getAddedSize() > 0) {
                    c.getAddedSubList().forEach(j -> {
                        if(!j.Build(this)) return;
                        if (j.body instanceof btRigidBody) collisionWorld.addRigidBody((btRigidBody) j.body);
                        else collisionWorld.addCollisionObject(j.body);
                        j.Constraints(collisionWorld::addConstraint);
                    });
                }

                if (c.getRemovedSize() > 0) {
                    c.getRemoved().forEach(j -> {
                        if(j.body == null) return;
                        if (j.body instanceof btRigidBody) collisionWorld.removeRigidBody((btRigidBody) j.body);
                        else collisionWorld.removeCollisionObject(j.body);
                        j.Constraints(collisionWorld::removeConstraint);
                    });
                }
            }
        });
    }

    public void Update() {
        Update(Gdx.graphics.getDeltaTime());
    }

    public void Update(float delta) {
        if(!simulatedJoints.isEmpty()) {
            collisionWorld.stepSimulation(delta, maxSubSteps, fixedTimeStep);
            simulatedJoints.forEach(j -> {
                Matrix4 worldTrans = new Matrix4();
                j.motionState.getWorldTransform(worldTrans);
                j.body.proceedToTransform(worldTrans);
            });
        }
    }

    public PhyJoint Add(SKJoint joint, PhysicsType type) {
        PhyJoint j = new PhyJoint(joint, type);
        simulatedJoints.add(j);
        return j;
    }

    public PhyJoint Get(SKJoint joint) {
        for (PhyJoint sj : simulatedJoints) {
            if(sj.joint.equals(joint))
                return sj;
        }
        return null;
    }

    public List<PhyJoint> AddChain(SKJoint joint) {
        List<PhyJoint> joints = new ArrayList<>();
        AddChain(joint, PhysicsType.STATIC, joints);
        return joints;
    }

    public void Remove(SKJoint joint) {
        for (PhyJoint simulatedJoint : simulatedJoints) {
            if(simulatedJoint.joint.equals(joint)) {
                simulatedJoints.remove(simulatedJoint);
                return;
            }
        }
    }

    protected void AddChain(SKJoint joint, PhysicsType type, List<PhyJoint> joints) {
        joints.add(Add(joint, type));
        joint.GetChildJoints().forEach(j -> AddChain(j, PhysicsType.DYNAMIC, joints));
    }

    public void RemoveChain(SKJoint joint) {
        Remove(joint);
        joint.GetChildJoints().forEach(this::RemoveChain);
    }

    public PhyJoint Toggle(SKJoint joint, PhysicsType type) {
        if(IsJointSimulated(joint))
            Remove(joint);
        else return Add(joint, type);
        return null;
    }

    public List<PhyJoint> ToggleChain(SKJoint joint) {
        if(IsJointSimulated(joint))
            RemoveChain(joint);
        else return AddChain(joint);
        return null;
    }

    public boolean IsJointSimulated(SKJoint joint) {
        for (PhyJoint simulatedJoint : simulatedJoints) {
            if(simulatedJoint.joint.equals(joint))
                return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        simulatedJoints.clear();

        collisionWorld.dispose();
        if(solver != null) solver.dispose();
        if(broadphase != null) broadphase.dispose();
        if(dispatcher != null) dispatcher.dispose();
        if(collisionConfiguration != null) collisionConfiguration.dispose();
    }

    public void DebugMode (final int mode) {
        if (mode == btIDebugDraw.DebugDrawModes.DBG_NoDebug && debugDrawer == null) return;
        if (debugDrawer == null) collisionWorld.setDebugDrawer(debugDrawer = new DebugDrawer());
        debugDrawer.setDebugMode(mode);
    }

    public int DebugMode () {
        return (debugDrawer == null) ? 0 : debugDrawer.getDebugMode();
    }

    public void DebugDraw(Batch batch, Camera camera) {
        if(debugDrawer != null && DebugMode() > 0) {
            batch.flush();
            debugDrawer.begin(camera);
            collisionWorld.debugDrawWorld();
            debugDrawer.end();
        }
    }

}