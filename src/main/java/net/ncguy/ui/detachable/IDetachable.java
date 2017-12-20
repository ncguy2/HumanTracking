package net.ncguy.ui.detachable;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.Optional;

public interface IDetachable {

    void SetParent(IDetachableContainer parent);
    Optional<IDetachableContainer> GetParent();
    Optional<Table> GetRootTable();

    default String GetTitle() {
        return getClass().getSimpleName();
    }

}
