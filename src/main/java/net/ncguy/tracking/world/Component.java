package net.ncguy.tracking.world;

import java.nio.ByteBuffer;
import java.util.List;

public abstract class Component {

    public transient Entity owningEntity;

    public Entity Entity() {
        return this.owningEntity;
    }

    public boolean IsAttached() {
        return owningEntity != null;
    }

    public void Attach(Entity entity) {
        if(IsAttached())
            Detach();
        this.owningEntity = entity;
        OnAttach();
    }

    public void Detach() {
        OnDetach();
        this.owningEntity = null;
    }

    public void OnAttach() {}
    public void OnDetach() {}

    public abstract void Serialize(List<Byte> bytes);
    public abstract void Deserialize(ByteBuffer buffer);

}
