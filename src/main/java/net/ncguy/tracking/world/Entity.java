package net.ncguy.tracking.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Entity {

    public Map<Class<? extends Component>, Component> components;

    public Entity() {
        components = new HashMap<>();
    }

    public void Set(Component... components) {
        for (Component component : components) {
            Class<? extends Component> type = component.getClass();
            Get(type).ifPresent(Component::Detach);
            this.components.put(type, component);
            component.Attach(this);
        }
    }

    public <T extends Component> Optional<T> Get(Class<T> cls) {
        Component component = components.get(cls);
        if(cls.isInstance(component))
            return Optional.of((T) component);
        return Optional.empty();
    }

    public boolean Any(Class<? extends Component>... classes) {
        Set<Class<? extends Component>> keySet = components.keySet();
        for (Class<? extends Component> cls : classes) {
            if(keySet.contains(cls))
                return true;
        }
        return false;
    }

    public boolean All(Class<? extends Component>... classes) {
        Set<Class<? extends Component>> keySet = components.keySet();
        for (Class<? extends Component> cls : classes) {
            if(!keySet.contains(cls))
                return false;
        }
        return true;
    }

    public void Clear() {
        components.values().forEach(Component::Detach);
        components.clear();
    }


}
