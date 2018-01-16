package net.ncguy.ui.detachable;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.MenuItem;

public interface IPanel {

    default void Init() {
        InitUI();
        AttachListeners();
        Assemble();
        Style();
    }

    void InitUI();
    void AttachListeners();
    void Assemble();
    void Style();

    default String GetTooltip() {
        return "";
    }

    String GetTitle();
    Table GetRootTable();

    default public void AddManagedMenuName(Class<?> owner, String name) {}
    default public void AddManagedMenuItem(Class<?> owner, MenuItem item) {}
    default public void RemoveManagedItems(Class<?> owner) {}

}
