package net.ncguy.physics.sim;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;

public enum PhysicsType {
    STATIC(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT),
    KINEMATIC(btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT),
    DYNAMIC(btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK),
    ;

    public final int flag;
    PhysicsType(int flag) {
        this.flag = flag;
    }

}
