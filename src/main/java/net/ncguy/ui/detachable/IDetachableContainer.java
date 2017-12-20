package net.ncguy.ui.detachable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public interface IDetachableContainer {


    default <T extends IDetachableContainer> Optional<T> To(Class<T> type) {
        try {

            final Constructor<T> ctor = type.getConstructor();
            final T t = ctor.newInstance();

            this.GetContents().ifPresent(c -> {
                c.GetRootTable().ifPresent(Actor::remove);
                c.GetParent().ifPresent(p -> p.OnRemove(c));
                t.SetContents(c);
            });

            return Optional.of(t);

        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    void OnRemove(IDetachable removed);

    default float GetMetric() {
        return 0.f;
    }

    Optional<IDetachable> GetContents();
    void SetContents(IDetachable contents);

    public static class Globals {
        public static boolean isDragging = false;
        public static Vector2 draggingTarget = new Vector2();
        public static BiConsumer<IDetachableContainer, Vector2> handleDrop;
        public static Set<DetachableTabContainer> detachableTabContainers = new LinkedHashSet<>();
    }

}
